package com.share.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.core.domain.R;
import com.share.common.core.exception.ServiceException;
import com.share.common.rabbit.constant.MqConst;
import com.share.common.rabbit.service.RabbitService;
import com.share.order.api.RemoteOrderInfoService;
import com.share.order.domain.OrderInfo;
import com.share.payment.domain.PaymentInfo;
import com.share.payment.mapper.PaymentInfoMapper;
import com.share.payment.service.IPaymentInfoService;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements IPaymentInfoService {

   @Autowired
   private PaymentInfoMapper paymentInfoMapper;

   @Resource
   private RemoteOrderInfoService remoteOrderInfoService;

   @Resource
   private RabbitService rabbitService;

   @Override
   public PaymentInfo savePaymentInfo(String orderNo) {
      PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));
      if(null == paymentInfo) {
         R<OrderInfo> orderInfoResult = remoteOrderInfoService.getByOrderNo(orderNo);
         if (R.FAIL == orderInfoResult.getCode()) {
            throw new ServiceException(orderInfoResult.getMsg());
         }
         OrderInfo orderInfo = orderInfoResult.getData();

         paymentInfo = new PaymentInfo();
         paymentInfo.setUserId(orderInfo.getUserId());
         paymentInfo.setContent("共享充电宝租借");
         paymentInfo.setAmount(orderInfo.getTotalAmount());
         paymentInfo.setOrderNo(orderNo);
         paymentInfo.setPaymentStatus(0);
         paymentInfoMapper.insert(paymentInfo);
      }
      if(paymentInfo.getPaymentStatus().intValue() == -1) {
         throw new ServiceException("订单已关闭");
      }
      return paymentInfo;
   }

   @Transactional(rollbackFor = Exception.class)
   @Override
   public void updatePaymentStatus(Transaction transaction) {
      PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, transaction.getOutTradeNo()));
      //1.已支付，直接返回
      if (paymentInfo.getPaymentStatus() == 1) {
         return;
      }

      //更新支付信息
      paymentInfo.setPaymentStatus(1);
      paymentInfo.setOrderNo(transaction.getOutTradeNo());
      paymentInfo.setTransactionId(transaction.getTransactionId());
      paymentInfo.setCallbackTime(new Date());
      paymentInfo.setCallbackContent(com.alibaba.fastjson.JSON.toJSONString(transaction));
      this.updateById(paymentInfo);

      //基于MQ通知订单系统，修改订单状态
      rabbitService.sendMessage(MqConst.EXCHANGE_PAYMENT_PAY, MqConst.ROUTING_PAYMENT_PAY, paymentInfo.getOrderNo());
   }

}