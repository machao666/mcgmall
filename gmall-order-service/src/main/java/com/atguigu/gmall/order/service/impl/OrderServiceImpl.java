package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置创建时间
        orderInfo.setCreateTime(new Date());

        Calendar calendar = Calendar.getInstance();
        //时间加一赋给清除时间
        calendar.add(Calendar.DATE, 1);

        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付号码
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);

        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0) {
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString();

        String tradeNoKey = "user:" + userId + ":tradeCode";

        Jedis jedis = redisUtil.getJedis();

        jedis.setex(tradeNoKey, 60 * 10, tradeNo);

        jedis.close();

        return tradeNo;
    }

    @Override
    public boolean checkedTradeNo(String userId, String tradeCodeNo) {

        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey = "user:" + userId + ":tradeCode";

        String tradeNo = jedis.get(tradeNoKey);

        jedis.close();

        if (tradeNo != null && tradeNo.equals(tradeCodeNo)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void delTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();

        String tradeNoKey = "user:" + userId + ":tradeCode";

        jedis.del(tradeNoKey);

        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId="+ skuId +"&num"+ skuNum);

        if("1".equals(result)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetail = new OrderDetail();

        orderDetail.setOrderId(orderId);

        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;
    }
}
