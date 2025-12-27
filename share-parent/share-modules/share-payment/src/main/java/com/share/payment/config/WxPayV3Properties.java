package com.share.payment.config;

import com.wechat.pay.java.core.RSAPublicKeyConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="wx.v3pay") //读取节点
@Data
public class WxPayV3Properties {

    private String appid;
    /** 商户号 */
    public String merchantId;
    /** 商户API私钥路径 */
    public String privateKeyPath;
    /** 商户API公钥路径 */
    public String publicKeyPath;
    /** 商户API公钥ID */
    public String publicKeyId;
    /** 商户证书序列号 */
    public String merchantSerialNumber;
    /** 商户APIV3密钥 */
    public String apiV3key;
    /** 回调地址 */
    private String notifyUrl;

//    @Bean
//    public RSAAutoCertificateConfig getConfig(){
//
//        PrivateKey privateKey = PemUtil.loadPrivateKeyFromPath(this.getPrivateKeyPath());
//        System.out.println("==============="+privateKey.toString());
//
//        return new RSAAutoCertificateConfig.Builder()
//                        .merchantId(this.getMerchantId())
//                        .privateKeyFromPath(this.getPrivateKeyPath())
//                        .merchantSerialNumber(this.getMerchantSerialNumber())
//                        .apiV3Key(this.getApiV3key())
//                        .build();
//
//    }

    @Bean
    public RSAPublicKeyConfig getPublicKeyConfig(){
        return new RSAPublicKeyConfig.Builder()
                .merchantId(this.getMerchantId()) //微信支付的商户号
                .privateKeyFromPath(this.getPrivateKeyPath()) // 商户API证书私钥的存放路径
                .publicKeyFromPath(this.getPublicKeyPath()) //微信支付公钥的存放路径
                .publicKeyId(this.publicKeyId) //微信支付公钥ID
                .merchantSerialNumber(this.getMerchantSerialNumber()) //商户API证书序列号
                .apiV3Key(this.getApiV3key()) //APIv3密钥
                .build();
    }
}