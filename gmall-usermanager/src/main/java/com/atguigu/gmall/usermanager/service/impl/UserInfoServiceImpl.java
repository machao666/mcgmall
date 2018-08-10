package com.atguigu.gmall.usermanager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanager.mapper.UserInfoMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;


import java.util.List;
@Service
public class UserInfoServiceImpl implements UserInfoService{

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60;

    @Override
    public List<UserInfo> getAllUserInfo() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());

        userInfo.setPasswd(password);

        UserInfo  info = userInfoMapper.selectOne(userInfo);

        if(info!=null){
            Jedis jedis = redisUtil.getJedis();

            String key = userKey_prefix+info.getId() +userinfoKey_suffix;

            String userJson = JSON.toJSONString(info);

            jedis.setex(key,userKey_timeOut,userJson);

            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        // 去缓存中查询是否有redis
        Jedis jedis = redisUtil.getJedis();
        String key = userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(key);
        // 延长时效
        jedis.expire(key,userKey_timeOut);
        if (userJson!=null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return  userInfo;
        }
        return  null;
    }

}
