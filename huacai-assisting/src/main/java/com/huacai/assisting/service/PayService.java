package com.huacai.assisting.service;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

@Service  // 加这一行
public interface PayService {
    /** 发起支付，返回支付宝自动提交表单 HTML */
    String pay(String orderId) throws Exception;

    /** 处理支付宝异步回调 */
    String handleNotify(HttpServletRequest request) throws Exception;

    String queryAndUpdate(String orderId) throws Exception;
}
