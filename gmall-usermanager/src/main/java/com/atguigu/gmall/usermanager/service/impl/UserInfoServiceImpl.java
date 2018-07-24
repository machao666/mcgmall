package com.atguigu.gmall.usermanager.service.impl;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.usermanager.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
