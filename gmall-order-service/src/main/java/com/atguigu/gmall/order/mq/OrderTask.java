package com.atguigu.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {

    @Autowired
    OrderService orderService;


    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
        System.out.println("---------------------------------------------------");
        List<OrderInfo> orderInfoList = orderService.getExpiredOrderList();
        for (OrderInfo orderInfo : orderInfoList) {

            // 处理未完成订单
           orderService.execExpiredOrder(orderInfo);
        }
    }
}
