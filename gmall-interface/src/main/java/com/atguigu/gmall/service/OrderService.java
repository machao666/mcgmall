package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {
   // 根据orderInfo保存orderInfo和orderDetail返回orderId
     String  saveOrder(OrderInfo orderInfo);

     String getTradeNo(String userId);

     boolean checkedTradeNo(String userId,String tradeCodeNo);

     void delTradeNo(String userId);

    boolean checkStock(String skuId,Integer skuNum);

    OrderInfo getOrderInfo(String orderId);
}
