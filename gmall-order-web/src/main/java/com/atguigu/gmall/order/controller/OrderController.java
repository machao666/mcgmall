package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserAddressService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserAddressService userAddressService;

    @RequestMapping("findAddress")
    @ResponseBody
    public List<UserAddress> findAddress(HttpServletRequest request){
        String id = request.getParameter("id");

        List<UserAddress> userAddressById = userAddressService.getUserAddressById(id);

        return userAddressById;


    }
}

