package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserAddressService;
import com.atguigu.gmall.util.annotation.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserAddressService userAddressService;
    @Reference
    CartService cartService;
    @Reference
    OrderService orderService;

    @RequestMapping("findAddress")
    @ResponseBody
    public List<UserAddress> findAddress(HttpServletRequest request) {
        String id = request.getParameter("id");

        List<UserAddress> userAddressById = userAddressService.getUserAddressById(id);

        return userAddressById;

    }

    @RequestMapping("trade")
    @LoginRequire
    public String tradeInit(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");

        List<UserAddress> userAddressList = userAddressService.getUserAddressById(userId);

        request.setAttribute("userAddressList", userAddressList);

        List<CartInfo> cartInfoList = cartService.getCheckedCart(userId);
        if(cartInfoList==null||cartInfoList.size()==0){
            request.setAttribute("errMsg","你还没有选择商品！！！");
            return "tradeFail";
        }
        List<OrderDetail> orderDetailList = new ArrayList<>(cartInfoList.size());

        if (cartInfoList != null && cartInfoList.size() > 0) {

            for (CartInfo cartInfo : cartInfoList) {
                OrderDetail orderDetail = new OrderDetail();

                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());

                orderDetailList.add(orderDetail);
            }
            request.setAttribute("orderDetailList", orderDetailList);
        }
        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setOrderDetailList(orderDetailList);

        orderInfo.sumTotalAmount();

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        String tradeNo = orderService.getTradeNo(userId);

        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }
    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){

        String tradeNo = request.getParameter("tradeNo");

        String userId = (String) request.getAttribute("userId");

        boolean flag = orderService.checkedTradeNo(userId, tradeNo);

        if(!flag){
            request.setAttribute("errMsg","该页面已经失效，请重新结算！！！");

            return "tradeFail";
        }
        orderService.delTradeNo(userId);
        //订单状态未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //进度状态未支付
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        //设置总价
        orderInfo.sumTotalAmount();

        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());

            if(!result){
                request.setAttribute("errMsg","商品库存不足，请重新下单！");
                return "tradeFail";
            }
        }

        String orderId = orderService.saveOrder(orderInfo);

        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
    @RequestMapping("list")
    public String list(){
        return "list";
    }
}
