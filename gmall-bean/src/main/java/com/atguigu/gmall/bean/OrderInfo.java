package com.atguigu.gmall.bean;

import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.PaymentWay;
import com.atguigu.gmall.enums.ProcessStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String consignee;

    @Column
    private String consigneeTel;

    @Column
    private BigDecimal totalAmount;

    @Column
    private OrderStatus orderStatus;

    @Column
    private ProcessStatus processStatus;

    @Column
    private String userId;

    @Column
    private PaymentWay paymentWay;

    @Column
    private Date expireTime;

    @Column
    private String deliveryAddress;

    @Column
    private String orderComment;

    @Column
    private Date createTime;

    @Column
    private String parentOrderId;

    @Column
    private String trackingNo;

    @Transient
    private List<OrderDetail> orderDetailList;

    @Transient
    private List<OrderInfo> orderSubList;

    @Transient
    private String wareId;

    @Column
    private String outTradeNo;

    @Override
    public String toString() {
        return "OrderInfo{" +
                "id='" + id + '\'' +
                ", consignee='" + consignee + '\'' +
                ", consigneeTel='" + consigneeTel + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderStatus=" + orderStatus +
                ", processStatus=" + processStatus +
                ", userId='" + userId + '\'' +
                ", paymentWay=" + paymentWay +
                ", expireTime=" + expireTime +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", orderComment='" + orderComment + '\'' +
                ", createTime=" + createTime +
                ", parentOrderId='" + parentOrderId + '\'' +
                ", trackingNo='" + trackingNo + '\'' +
                ", orderDetailList=" + orderDetailList +
                ", orderSubList=" + orderSubList +
                ", wareId='" + wareId + '\'' +
                ", outTradeNo='" + outTradeNo + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getConsigneeTel() {
        return consigneeTel;
    }

    public void setConsigneeTel(String consigneeTel) {
        this.consigneeTel = consigneeTel;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public ProcessStatus getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getOrderComment() {
        return orderComment;
    }

    public void setOrderComment(String orderComment) {
        this.orderComment = orderComment;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public List<OrderDetail> getOrderDetailList() {
        return orderDetailList;
    }

    public void setOrderDetailList(List<OrderDetail> orderDetailList) {
        this.orderDetailList = orderDetailList;
    }

    public List<OrderInfo> getOrderSubList() {
        return orderSubList;
    }

    public void setOrderSubList(List<OrderInfo> orderSubList) {
        this.orderSubList = orderSubList;
    }

    public String getWareId() {
        return wareId;
    }

    public void setWareId(String wareId) {
        this.wareId = wareId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public void sumTotalAmount(){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount= totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount=  totalAmount;
    }
    public String getTradeBody(){
        OrderDetail orderDetail = orderDetailList.get(0);
        String tradeBody=orderDetail.getSkuName()+"等"+orderDetailList.size()+"件商品";
        return tradeBody;
    }
}
