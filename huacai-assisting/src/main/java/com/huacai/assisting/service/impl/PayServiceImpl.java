package com.huacai.assisting.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {
    private static final String STATUS_PENDING_DELIVERY = "待发货";
    private static final String STATUS_PENDING_RECEIPT = "待收货";
    private static final String STATUS_COMPLETED = "已完成";
    private static final String STATUS_CANCELLED = "已取消";

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final OrdersMapper ordersMapper;
    private final IProductsService productsService;
    private final ISysUserService sysUserService;

    @Override
    public String pay(String orderId) throws Exception {
        String diagId = buildDiagId();
        try {
            validateAlipayConfig();
            Orders order = ordersMapper.selectOrdersByOrdersId(orderId);
            validateOrderBeforePay(orderId, order);
            preflightGateway(diagId);

            BigDecimal payable = order.getTotalPrice().setScale(2, RoundingMode.HALF_UP);

            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(buildReturnUrl(order.getOrdersId()));
            request.setBizContent("{"
                    + "\"out_trade_no\":\"" + order.getOrdersId() + "\","
                    + "\"total_amount\":\"" + payable.toPlainString() + "\","
                    + "\"subject\":\"助农商城-订单" + order.getOrdersId() + "\","
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                    + "}");

            AlipayTradePagePayResponse payResponse = alipayClient.pageExecute(request);
            String form = payResponse == null ? null : payResponse.getBody();
            if (form == null || !form.contains("<form")) {
                String subMsg = payResponse == null ? "无响应" : payResponse.getSubMsg();
                log.error("发起支付失败，diagId={}, 订单ID={}, subMsg={}", diagId, orderId, subMsg);
                throw new RuntimeException("支付网关返回异常（诊断ID: " + diagId + "）");
            }
            log.info("发起支付宝支付成功，diagId={}, 订单ID={}, amount={}", diagId, orderId, payable);
            return form;
        } catch (Exception e) {
            // 若已包含诊断ID则直接抛出，避免重复包裹
            if (e.getMessage() != null && e.getMessage().contains("诊断ID")) {
                throw e;
            }
            log.error("支付前检查失败，diagId={}, 订单ID={}, 错误={}", diagId, orderId, e.getMessage(), e);
            throw new RuntimeException((e.getMessage() == null ? "支付失败" : e.getMessage()) + "（诊断ID: " + diagId + "）");
        }
    }

    /**
     * 支付前进行网关可用性探测：
     * - 20000：网关可用（成功）
     * - 40004：业务订单不存在（探测单号本就不存在，说明链路可用）
     */
    private void preflightGateway(String diagId) {
        String probeOrderId = "DIAG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        try {
            AlipayTradeQueryRequest probeReq = new AlipayTradeQueryRequest();
            probeReq.setBizContent("{\"out_trade_no\":\"" + probeOrderId + "\"}");
            AlipayTradeQueryResponse probeResp = alipayClient.execute(probeReq);
            if (probeResp == null) {
                throw new RuntimeException("网关探测无响应");
            }
            String code = probeResp.getCode();
            String subCode = probeResp.getSubCode();
            if (!"20000".equals(code) && !"40004".equals(code)) {
                throw new RuntimeException("网关探测异常，code=" + code + ", subCode=" + subCode);
            }
            log.info("支付前网关探测通过，diagId={}, probeOrderId={}, code={}, subCode={}",
                    diagId, probeOrderId, code, subCode);
        } catch (Exception ex) {
            String raw = ex.getMessage() == null ? "未知错误" : ex.getMessage();
            log.error("支付前网关探测失败，diagId={}, probeOrderId={}, error={}", diagId, probeOrderId, raw, ex);
            throw new RuntimeException("支付网关预检查失败（诊断ID: " + diagId + "，探测单号: " + probeOrderId + "，原因: " + raw + "）");
        }
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

    private void validateAlipayConfig() {
        if (isBlank(alipayConfig.getAppId())
                || isBlank(alipayConfig.getMerchantPrivateKey())
                || isBlank(alipayConfig.getAlipayPublicKey())
                || isBlank(alipayConfig.getNotifyUrl())
                || isBlank(alipayConfig.getGatewayUrl())) {
            throw new RuntimeException("支付配置不完整，请联系管理员");
        }
    }

    private void validateOrderBeforePay(String orderId, Orders order) {
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }
        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("订单金额异常，请刷新后重试");
        }
        String status = order.getStatus();
        if (isOrderAlreadyProcessed(status)) {
            throw new RuntimeException("订单已支付，无需重复支付");
        }
        if (STATUS_CANCELLED.equals(status)) {
            throw new RuntimeException("订单已取消，无法发起支付");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String buildDiagId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private boolean isOrderAlreadyProcessed(String status) {
        return STATUS_PENDING_DELIVERY.equals(status)
                || STATUS_PENDING_RECEIPT.equals(status)
                || STATUS_COMPLETED.equals(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderPaid(String orderId) {
        // 1) 查询订单与订单明细（包含 ordersProductsList）
        Orders order = ordersMapper.selectOrdersByOrdersId(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        // 幂等保护：订单进入已支付后的任一阶段，都不再重复扣库存或重复入账。
        if (isOrderAlreadyProcessed(order.getStatus())) {
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
        updateOrders.setStatus(STATUS_PENDING_DELIVERY);
        ordersMapper.updateOrders(updateOrders);
    }
}
