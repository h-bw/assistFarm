package com.huacai.assisting.controller;

import com.huacai.assisting.service.PayService;
import com.huacai.common.core.domain.AjaxResult;
import com.huacai.common.annotation.Anonymous;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@Anonymous
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 发起支付
     * 前端访问：GET /api/pay/{orderId}
     * 后端返回支付宝跳转表单，浏览器自动提交
     */
    @GetMapping("/{orderId}")
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
}
