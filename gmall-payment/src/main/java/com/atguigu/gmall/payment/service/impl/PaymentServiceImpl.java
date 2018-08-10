package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        PaymentInfo paymentInfo1 = new PaymentInfo();

        paymentInfo1.setOutTradeNo(paymentInfo.getOutTradeNo());

        List<PaymentInfo> paymentInfos = paymentInfoMapper.select(paymentInfo1);

        if(paymentInfos.size()>0){
            return ;
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

        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd,example);




    }
}
