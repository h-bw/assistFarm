package com.huacai.assisting.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.huacai.assisting.domain.Orders;
import com.huacai.assisting.mapper.OrdersMapper;
import com.huacai.common.config.AlipayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.huacai.assisting.service.PayService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final OrdersMapper ordersMapper;

    @Override
    public String pay(String orderId) throws Exception {
        // 查询订单
        Orders order = ordersMapper.selectOrdersByOrdersId(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        request.setBizContent("{"
                + "\"out_trade_no\":\"" + order.getOrdersId() + "\","
                + "\"total_amount\":\"" + order.getTotalPrice() + "\","
                + "\"subject\":\"助农商城-订单" + order.getOrdersId() + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + "}");

        String form = alipayClient.pageExecute(request).getBody();
        log.info("发起支付宝支付，订单ID: {}", orderId);
        return form;
    }

    @Override
    public String handleNotify(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
        );

        if (!signVerified) {
            log.warn("支付宝回调验签失败！params: {}", params);
            return "fail";
        }

        String tradeStatus = params.get("trade_status");
        String outTradeNo  = params.get("out_trade_no");
        log.info("支付回调: orderId={}, status={}", outTradeNo, tradeStatus);

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            Orders order = new Orders();
            order.setOrdersId(outTradeNo);     // String 类型，直接赋值
            order.setStatus("待发货");          // 支付成功后流转到待发货
            ordersMapper.updateOrders(order);
            log.info("订单 {} 支付成功，状态已更新为待发货", outTradeNo);
        }

        return "success";
    }
}