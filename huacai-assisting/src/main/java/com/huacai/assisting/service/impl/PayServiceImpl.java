package com.huacai.assisting.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.huacai.assisting.domain.Orders;
import com.huacai.assisting.mapper.OrdersMapper;
import com.huacai.assisting.service.PayService;
import com.huacai.common.config.AlipayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        Orders order = ordersMapper.selectOrdersByOrdersId(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(buildReturnUrl(order.getOrdersId()));
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
        String outTradeNo = params.get("out_trade_no");
        log.info("支付回调: orderId={}, status={}", outTradeNo, tradeStatus);

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            markOrderPaid(outTradeNo);
            log.info("订单 {} 支付成功，状态已更新为待发货", outTradeNo);
        }

        return "success";
    }

    @Override
    public String queryAndUpdate(String orderId) throws Exception {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + orderId + "\"}");

        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response == null) {
            log.warn("支付结果查询失败，orderId={}", orderId);
            return "query_failed";
        }

        String tradeStatus = response.getTradeStatus();
        log.info("支付查询: orderId={}, status={}", orderId, tradeStatus);
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            markOrderPaid(orderId);
            return "paid";
        }

        return tradeStatus == null ? "not_paid" : tradeStatus;
    }

    private String buildReturnUrl(String orderId) {
        String returnUrl = alipayConfig.getReturnUrl();
        if (returnUrl == null || returnUrl.isBlank()) {
            returnUrl = "";
        }
        if (returnUrl.contains("?")) {
            return returnUrl + "&orderId=" + orderId;
        }
        return returnUrl + "?orderId=" + orderId;
    }

    private void markOrderPaid(String orderId) {
        Orders order = new Orders();
        order.setOrdersId(orderId);
        order.setStatus("待发货");
        ordersMapper.updateOrders(order);
    }
}
