package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.enums.PaymentStatus;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        PaymentInfo paymentInfo1 = new PaymentInfo();

        paymentInfo1.setOutTradeNo(paymentInfo.getOutTradeNo());

        List<PaymentInfo> paymentInfos = paymentInfoMapper.select(paymentInfo1);

        if (paymentInfos.size() > 0) {
            return;
        }

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getpaymentInfo(PaymentInfo paymentInfo) {

        return paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {
        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd, example);


    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();

            activeMQMapMessage.setString("orderId", paymentInfo.getOrderId());

            activeMQMapMessage.setString("result", result);

            producer.send(activeMQMapMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        PaymentInfo paymentInfo = getpaymentInfo(paymentInfoQuery);

        if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID || paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED) {
            return true;
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + paymentInfo.getOutTradeNo() + "\"" +
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())) {
                System.out.println("支付成功");
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(), paymentInfoUpd);
                sendPaymentResult(paymentInfo, "success");
                return true;
            } else {
                System.out.println("支付失败");
                return false;
            }
        } else {
            System.out.println("交易失败");
            return false;
        }
    }

    @Override
    public void closePayment(String orderId) {
        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);

        Example example = new Example(PaymentInfo.class);

        example.createCriteria().andEqualTo("orderId", orderId);

        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
    }

}
