package com.atguigu.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentResultCheck(MapMessage mapMessage) throws JMSException {

        String outTradeNo = mapMessage.getString("outTradeNo");

        int delaySec = mapMessage.getInt("delaySec");

        int checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOutTradeNo(outTradeNo);

        System.out.println("开始检查");

        boolean flag = paymentService.checkPayment(paymentInfo);

        if (!flag && checkCount > 0) {
            System.out.println("再次发送！！！");

            orderService.sendDelayPaymentResult(outTradeNo, delaySec, checkCount - 1);
        }
    }
}
