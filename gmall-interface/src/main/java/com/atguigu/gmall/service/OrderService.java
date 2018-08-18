package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
   // 根据orderInfo保存orderInfo和orderDetail返回orderId
     String  saveOrder(OrderInfo orderInfo);

     String getTradeNo(String userId);

     boolean checkedTradeNo(String userId,String tradeCodeNo);

     void delTradeNo(String userId);

    boolean checkStock(String skuId,Integer skuNum);

    OrderInfo getOrderInfo(String orderId);

    void updateOrderStatus(String orderId, ProcessStatus paid);

    void sendOrderStatus(String orderId);

    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

    void execExpiredOrder(OrderInfo orderInfo);


    List<OrderInfo> getExpiredOrderList();

    List<OrderInfo> spiltOrder(String orderId, String wareSkuMap);

    Map initWareOrder(OrderInfo orderInfo);
}
