package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.enums.PaymentStatus;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
public class PaymentController {

    @Reference
    OrderService orderService;
    @Reference
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(HttpServletRequest request){

        String orderId = request.getParameter("orderId");

         OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("orderId",orderId);

        return "index";
    }

    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request , HttpServletResponse response){

        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOrderId(orderId);

        paymentInfo.setCreateTime(new Date());

        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());

        paymentInfo.setSubject(orderInfo.getTradeBody());

        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());

        paymentService.savePayment(paymentInfo);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        Map<String,Object> map = new HashMap<>();

        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("subject",paymentInfo.getSubject());
        map.put("total_amount",paymentInfo.getTotalAmount());

        String json = JSON.toJSONString(map);

        alipayRequest.setBizContent(json);

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        return form;
    }

    @RequestMapping(value = "alipay/callback/return",method = RequestMethod.GET)
    public String callBackReturn(){
        return "redirect://"+AlipayConfig.return_order_url;
    }

    @RequestMapping(value = "alipay/callback/return",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap,HttpServletRequest request){
        String sign = request.getParameter("sign");

        try {
            boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);

            if(!flag){
                return "fail";
            }
            String trade_status = paramMap.get("trade_status");

            if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                String out_trade_no = paramMap.get("out_trade_no");

                PaymentInfo paymentInfo = new PaymentInfo();

                paymentInfo.setOutTradeNo(out_trade_no);

                PaymentInfo paymentInfoHas = paymentService.getpaymentInfo(paymentInfo);

                if(paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID||paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){
                    return "fail";
                }else{
                    PaymentInfo paymentInfoUpd = new PaymentInfo();

                    paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);

                    // 设置创建时间
                    paymentInfoUpd.setCallbackTime(new Date());
                    // 设置内容
                    paymentInfoUpd.setCallbackContent(paramMap.toString());

                    paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);

                    return "success";
                }
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return "fail";
    }

}
