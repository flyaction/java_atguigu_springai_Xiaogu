package com.share.payment.service.impl;

import com.alibaba.fastjson2.JSON;
import com.share.common.core.domain.R;
import com.share.common.core.utils.StringUtils;
import com.share.common.core.utils.bean.BeanUtils;
import com.share.payment.config.WxPayV3Properties;
import com.share.payment.domain.CreateWxPaymentForm;
import com.share.payment.domain.PaymentInfo;
import com.share.payment.domain.WxPrepayVo;
import com.share.payment.service.IPaymentInfoService;
import com.share.payment.service.IWxPayService;
import com.share.payment.util.RequestUtils;
import com.share.user.api.RemoteUserInfoService;
import com.share.user.domain.UserInfo;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class WxPayServiceImpl implements IWxPayService {

   @Autowired
   private IPaymentInfoService paymentInfoService;

   @Resource
   private RemoteUserInfoService remoteUserInfoService;

   @Autowired
   private WxPayV3Properties wxPayV3Properties;

//   @Autowired
//   private RSAAutoCertificateConfig rsaAutoCertificateConfig;

   @Autowired
   private RSAPublicKeyConfig rsaPublicKeyConfig;



   @Override
   public WxPrepayVo createWxPayment(CreateWxPaymentForm createWxPaymentForm) {
      try {
         //保存支付记录
         PaymentInfo paymentInfo = paymentInfoService.savePaymentInfo(createWxPaymentForm.getOrderNo());

         //获取用户信息
         R<UserInfo> userInfoResult = remoteUserInfoService.getInfo(paymentInfo.getUserId());
         if (StringUtils.isNull(userInfoResult) || StringUtils.isNull(userInfoResult.getData())) {
            throw new com.share.common.core.exception.ServiceException("获取用户信息失败");
         }
         if (R.FAIL == userInfoResult.getCode()) {
            throw new com.share.common.core.exception.ServiceException(userInfoResult.getMsg());
         }
         String openid = userInfoResult.getData().getWxOpenId();

         // 构建service
         JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(rsaPublicKeyConfig).build();

         // request.setXxx(val)设置所需参数，具体参数可见Request定义
         PrepayRequest request = new PrepayRequest();
         Amount amount = new Amount();
         amount.setTotal(paymentInfo.getAmount().multiply(new BigDecimal(100)).intValue());
         request.setAmount(amount);
         request.setAppid(wxPayV3Properties.getAppid());
         request.setMchid(wxPayV3Properties.getMerchantId());
         request.setDescription(paymentInfo.getContent());
         request.setNotifyUrl(wxPayV3Properties.getNotifyUrl());
         request.setOutTradeNo(paymentInfo.getOrderNo());

         //获取用户信息
         Payer payer = new Payer();
         payer.setOpenid(openid);
         request.setPayer(payer);

         // 调用下单方法，得到应答
         // response包含了调起支付所需的所有参数，可直接用于前端调起支付
         PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
         log.info("微信支付下单返回参数：{}", JSON.toJSONString(response));

         WxPrepayVo wxPrepayVo = new WxPrepayVo();
         BeanUtils.copyProperties(response, wxPrepayVo);
         wxPrepayVo.setTimeStamp(response.getTimeStamp());

         return wxPrepayVo;
      } catch (ServiceException e) {
         e.printStackTrace();
         throw new com.share.common.core.exception.ServiceException(e.getErrorMessage());
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
         throw new com.share.common.core.exception.ServiceException("订单号不存在");
      } catch (Exception e) {
         e.printStackTrace();
         throw new com.share.common.core.exception.ServiceException("微信下单异常");
      }
   }

   @Override
   public void wxnotify(HttpServletRequest request) {
      //1.回调通知的验签与解密
      //从request头信息获取参数
      //HTTP 头 Wechatpay-Signature
      // HTTP 头 Wechatpay-Nonce
      //HTTP 头 Wechatpay-Timestamp
      //HTTP 头 Wechatpay-Serial
      //HTTP 头 Wechatpay-Signature-Type
      //HTTP 请求体 body。切记使用原始报文，不要用 JSON 对象序列化后的字符串，避免验签的 body 和原文不一致。
      String wechatPaySerial = request.getHeader("Wechatpay-Serial");
      String nonce = request.getHeader("Wechatpay-Nonce");
      String timestamp = request.getHeader("Wechatpay-Timestamp");
      String signature = request.getHeader("Wechatpay-Signature");
      String requestBody = RequestUtils.readData(request);
      log.info("wechatPaySerial：{}", wechatPaySerial);
      log.info("nonce：{}", nonce);
      log.info("timestamp：{}", timestamp);
      log.info("signature：{}", signature);
      log.info("requestBody：{}", requestBody);

      //2.构造 RequestParam
      RequestParam requestParam = new RequestParam.Builder()
              .serialNumber(wechatPaySerial)
              .nonce(nonce)
              .signature(signature)
              .timestamp(timestamp)
              .body(requestBody)
              .build();


      //3.初始化 NotificationParser
      NotificationParser parser = new NotificationParser(rsaPublicKeyConfig);
      //4.以支付通知回调为例，验签、解密并转换成 Transaction
      Transaction transaction = parser.parse(requestParam, Transaction.class);
      log.info("成功解析：{}", JSON.toJSONString(transaction));
      if(null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
         //5.处理支付业务
         paymentInfoService.updatePaymentStatus(transaction);
      }
   }

   @Override
   public Transaction queryPayStatus(String orderNo) {
      // 构建service
      JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(rsaPublicKeyConfig).build();

      QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
      queryRequest.setMchid(wxPayV3Properties.getMerchantId());
      queryRequest.setOutTradeNo(orderNo);

      try {
         Transaction result = service.queryOrderByOutTradeNo(queryRequest);
         log.info(JSON.toJSONString(result));
         return result;
      } catch (ServiceException e) {
         // API返回失败, 例如ORDER_NOT_EXISTS
         System.out.printf("code=[%s], message=[%s]\n", e.getErrorCode(), e.getErrorMessage());
         System.out.printf("reponse body=[%s]\n", e.getResponseBody());
      }
      return null;
   }

}