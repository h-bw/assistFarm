package com.huacai.assisting.controller;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.huacai.assisting.service.PayService;
import com.huacai.common.config.AlipayConfig;
import com.huacai.common.core.domain.AjaxResult;
import com.huacai.common.annotation.Anonymous;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@Anonymous
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;
    private final AlipayConfig alipayConfig;
    private final AlipayClient alipayClient;

    /**
     * 发起支付
     * 前端访问：GET /api/pay/{orderId}
     * 后端返回支付宝跳转表单，浏览器自动提交
     */
    @GetMapping({"/{orderId:^(?!diagnose$).+}", "/create/{orderId}"})
    public void pay(@PathVariable String orderId,
                    HttpServletResponse response) throws Exception {
        String form = payService.pay(orderId);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
    }

    /**
     * 支付宝异步回调（服务端通知）
     * 支付宝 POST 请求：/api/pay/notify
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) throws Exception {
        return payService.handleNotify(request);
    }

    /**
     * 前端支付回跳后的结果查询与同步
     * GET /api/pay/query/{orderId}
     */
    @GetMapping("/query/{orderId}")
    public AjaxResult query(@PathVariable String orderId) throws Exception {
        return AjaxResult.success(payService.queryAndUpdate(orderId));
    }

    /**
     * 支付沙箱自检
     * 用途：快速检查配置完整性、网关地址与基础连通性，排查 SYSTEM_ERROR
     */
    @GetMapping("/diagnose")
    public AjaxResult diagnose() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("appIdConfigured", notBlank(alipayConfig.getAppId()));
        data.put("privateKeyConfigured", notBlank(alipayConfig.getMerchantPrivateKey()));
        data.put("publicKeyConfigured", notBlank(alipayConfig.getAlipayPublicKey()));
        data.put("notifyUrlConfigured", notBlank(alipayConfig.getNotifyUrl()));
        data.put("returnUrlConfigured", notBlank(alipayConfig.getReturnUrl()));
        data.put("gatewayUrl", alipayConfig.getGatewayUrl());
        data.put("isSandboxGateway", containsIgnoreCase(alipayConfig.getGatewayUrl(), "alipaydev.com"));
        data.put("signType", alipayConfig.getSignType());
        data.put("charset", alipayConfig.getCharset());

        boolean configOk = notBlank(alipayConfig.getAppId())
                && notBlank(alipayConfig.getMerchantPrivateKey())
                && notBlank(alipayConfig.getAlipayPublicKey())
                && notBlank(alipayConfig.getNotifyUrl())
                && notBlank(alipayConfig.getGatewayUrl());
        data.put("configComplete", configOk);

        Map<String, Object> connectivity = new LinkedHashMap<>();
        if (configOk) {
            try {
                // 使用不存在的订单号做查询，核心目的是验证“请求能发出去且签名可被网关识别”
                String probeOrderId = "DIAG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                request.setBizContent("{\"out_trade_no\":\"" + probeOrderId + "\"}");
                AlipayTradeQueryResponse resp = alipayClient.execute(request);
                connectivity.put("requestSent", true);
                connectivity.put("code", resp == null ? null : resp.getCode());
                connectivity.put("msg", resp == null ? null : resp.getMsg());
                connectivity.put("subCode", resp == null ? null : resp.getSubCode());
                connectivity.put("subMsg", resp == null ? null : resp.getSubMsg());
                connectivity.put("bodyPresent", resp != null && resp.getBody() != null && !resp.getBody().isBlank());
            } catch (Exception e) {
                connectivity.put("requestSent", false);
                connectivity.put("error", e.getMessage());
            }
        } else {
            connectivity.put("requestSent", false);
            connectivity.put("error", "配置不完整，跳过网关探测");
        }
        data.put("gatewayProbe", connectivity);

        return AjaxResult.success(data);
    }

    private boolean notBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private boolean containsIgnoreCase(String src, String part) {
        return src != null && part != null && src.toLowerCase().contains(part.toLowerCase());
    }
}
