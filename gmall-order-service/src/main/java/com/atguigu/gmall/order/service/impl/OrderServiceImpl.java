package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;

import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    PaymentService paymentService;

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
        new ArrayList();
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

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        Connection connection = activeMQUtil.getConnection();

        String orderJson = initWareOrder(orderId);
        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");

            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = session.createTextMessage();

            textMessage.setText(orderJson);

            producer.send(textMessage);

            session.commit();

            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param outTradeNo 单号
     * @param delaySec  延迟秒
     * @param checkCount 几次
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();

            Session session = connection.createSession(true,Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();

            activeMQMapMessage.setString("outTradeNo",outTradeNo);

            activeMQMapMessage.setInt("delaySec",delaySec);

            activeMQMapMessage.setInt("checkCount",checkCount);

            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);

            producer.send(activeMQMapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);

        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }
    public Map initWareOrder(OrderInfo orderInfo) {

        Map<String,Object> map = new HashMap<>();

        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());

        List detailList = new ArrayList();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);

        return map ;
    }

    @Async
    public void execExpiredOrder(OrderInfo orderInfo) {
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);

        paymentService.closePayment(orderInfo.getId());
    }

    public List<OrderInfo> getExpiredOrderList() {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);
        return orderInfoMapper.selectByExample(example);
    }

    @Override
    public List<OrderInfo> spiltOrder(String orderId, String wareSkuMap) {
        List<OrderInfo> orderInfoList = new ArrayList<>();

        OrderInfo orderInfoQuery = getOrderInfo(orderId);

        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);

        for (Map map : mapList) {
            String  wareId = (String) map.get("wareId");

            List<String>  skuIds = (List<String>) map.get("skuIds");

            OrderInfo orderInfoSub = new OrderInfo();

            try {
                BeanUtils.copyProperties(orderInfoSub,orderInfoQuery);

                orderInfoSub.setId(null);

                orderInfoSub.setParentOrderId(orderInfoQuery.getId());

                orderInfoSub.setWareId(wareId);

                List<OrderDetail> orderDetailList = orderInfoQuery.getOrderDetailList();

                List<OrderDetail> orderDetails = new ArrayList<>();

                for (OrderDetail orderDetail : orderDetailList) {

                    for (String skuId : skuIds) {
                        if(orderDetail.getSkuId().equals(skuId)){
                            orderDetail.setId(null);

                            orderDetails.add(orderDetail);

                        }
                    }
                }
                orderInfoSub.setOrderDetailList(orderDetails);

                orderInfoSub.sumTotalAmount();

                saveOrder(orderInfoSub);

                orderInfoList.add(orderInfoSub);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            updateOrderStatus(orderId,ProcessStatus.SPLIT);
        }

        return orderInfoList;
    }

}
