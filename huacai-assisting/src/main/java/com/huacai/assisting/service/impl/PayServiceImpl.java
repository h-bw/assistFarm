package com.huacai.assisting.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.huacai.assisting.domain.OrdersProducts;
import com.huacai.assisting.domain.Orders;
import com.huacai.assisting.domain.Products;
import com.huacai.assisting.mapper.OrdersMapper;
import com.huacai.assisting.service.PayService;
import com.huacai.assisting.service.IProductsService;
import com.huacai.system.service.ISysUserService;
import com.huacai.common.config.AlipayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final OrdersMapper ordersMapper;
    private final IProductsService productsService;
    private final ISysUserService sysUserService;

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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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

    public void markOrderPaid(String orderId) {
        // 1) 查询订单与订单明细（包含 ordersProductsList）
        Orders order = ordersMapper.selectOrdersByOrdersId(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        // 幂等保护：若已是“待发货”，说明已处理过扣库存/入账
        if ("待发货".equals(order.getStatus())) {
            return;
        }

        BigDecimal totalPrice = order.getTotalPrice() == null ? BigDecimal.ZERO : order.getTotalPrice();
        Long productsUserId = order.getProductsUserId();

        // 2) 扣减每个订单明细对应的商品库存
        if (order.getOrdersProductsList() != null) {
            for (OrdersProducts ordersProducts : order.getOrdersProductsList()) {
                String productsId = ordersProducts.getProductsId();
                Long quantity = ordersProducts.getQuantity();

                if (productsId == null || quantity == null) {
                    throw new RuntimeException("订单明细数据异常，productsId/quantity 为空");
                }

                Products dbProduct = productsService.selectProductsByProductsId(productsId);
                if (dbProduct == null || dbProduct.getInventory() == null) {
                    throw new RuntimeException("商品库存查询失败: " + productsId);
                }

                BigDecimal inventory = dbProduct.getInventory();
                BigDecimal newInventory = inventory.subtract(BigDecimal.valueOf(quantity));

                if (newInventory.compareTo(BigDecimal.ZERO) < 0) {
                    // 库存不足：直接中止并回滚，避免出现“已支付但库存为负”
                    throw new RuntimeException("库存不足，商品ID: " + productsId);
                }

                Products updateProduct = new Products();
                updateProduct.setProductsId(productsId);
                updateProduct.setInventory(newInventory);
                productsService.updateProducts(updateProduct);
            }
        }

        // 3) 给农户增加账户余额
        if (productsUserId == null) {
            throw new RuntimeException("订单所属农户用户ID为空: " + orderId);
        }

        Double money = totalPrice.setScale(2, RoundingMode.HALF_UP).doubleValue();
        sysUserService.updateUserBalance(productsUserId, "increase", money);

        // 4) 更新订单状态为“待发货”
        Orders updateOrders = new Orders();
        updateOrders.setOrdersId(orderId);
        updateOrders.setStatus("待发货");
        ordersMapper.updateOrders(updateOrders);
    }
}
