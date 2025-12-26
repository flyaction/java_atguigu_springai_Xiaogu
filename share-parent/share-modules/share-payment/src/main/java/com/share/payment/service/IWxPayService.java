package com.share.payment.service;

import com.share.payment.domain.CreateWxPaymentForm;
import com.share.payment.domain.WxPrepayVo;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.servlet.http.HttpServletRequest;

public interface IWxPayService {

	WxPrepayVo createWxPayment(CreateWxPaymentForm createWxPaymentForm);

	void wxnotify(HttpServletRequest request);

	Transaction queryPayStatus(String orderNo);
}