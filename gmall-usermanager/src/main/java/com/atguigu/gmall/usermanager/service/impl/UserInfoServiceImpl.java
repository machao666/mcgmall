package com.atguigu.gmall.usermanager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanager.mapper.UserInfoMapper;

import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
@Service
public class UserInfoServiceImpl implements UserInfoService{

    @Autowired
    UserInfoMapper userInfoMapper;
    @Override
    public List<UserInfo> getAllUserInfo() {
        return userInfoMapper.selectAll();
    }
}
