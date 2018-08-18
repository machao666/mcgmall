package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService  {

    void savePayment(PaymentInfo paymentInfo);

    PaymentInfo getpaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    void sendPaymentResult(PaymentInfo paymentInfo,String result);

    boolean checkPayment(PaymentInfo paymentInfoQuery);

    void closePayment(String id);

}
