package com.share.payment.api;

import com.alibaba.fastjson.JSON;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.common.security.annotation.RequiresLogin;
import com.share.payment.domain.CreateWxPaymentForm;
import com.share.payment.domain.WxPrepayVo;
import com.share.payment.service.IPaymentInfoService;
import com.share.payment.service.IWxPayService;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "微信支付接口")
@RestController
@RequestMapping("/wxPay")
@Slf4j
public class WxPayApiController extends BaseController {

    @Autowired
    private IWxPayService wxPayService;

    @Autowired
    private IPaymentInfoService paymentInfoService;

    @RequiresLogin
    @Operation(summary = "微信下单")
    @PostMapping("/createWxPayment")
    public AjaxResult createWxPayment(@RequestBody CreateWxPaymentForm createWxPaymentForm) {
        WxPrepayVo wxPrepayVo = wxPayService.createWxPayment(createWxPaymentForm);
        return success(wxPrepayVo);
    }

    //https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_5.shtml
    @Operation(summary = "微信支付异步通知接口")
    @PostMapping("/notify")
    public Map<String, Object> notify(HttpServletRequest request) {
        try {
            wxPayService.wxnotify(request);

            //返回成功
            Map<String, Object> result = new HashMap<>();
            result.put("code", "SUCCESS");
            result.put("message", "成功");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回失败
        Map<String, Object> result = new HashMap<>();
        result.put("code", "FAIL");
        result.put("message", "失败");
        return result;
    }

    @RequiresLogin
    @Operation(summary = "支付状态查询")
    @GetMapping("/queryPayStatus/{orderNo}")
    public AjaxResult queryPayStatus(@PathVariable String orderNo) {
        try {
            //调用查询接口
            Transaction transaction = wxPayService.queryPayStatus(orderNo);
            System.out.println("queryPayStatus: " + JSON.toJSONString(transaction));
            if (null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                //更改订单状态
                paymentInfoService.updatePaymentStatus(transaction);
                return success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success(false);
    }

}