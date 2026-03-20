package com.huacai.common.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String merchantPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl;
    private String charset = "UTF-8";
    private String signType = "RSA2";

    @Bean
    public AlipayClient alipayClient() throws Exception {
        return new DefaultAlipayClient(
                gatewayUrl, appId, merchantPrivateKey,
                "json", charset, alipayPublicKey, signType
        );
    }
}