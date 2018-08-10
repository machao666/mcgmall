package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassPortController {

    @Value("${token.key}")
    private String singKey;

    @Reference
    UserInfoService userInfoService;

    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        String origin = request.getParameter("originUrl");

        request.setAttribute("originUrl", origin);

        return "index";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo) {
        String remoteAddr = request.getHeader("X-forwarded-for");

        if (userInfo != null) {
            UserInfo info = userInfoService.login(userInfo);

            if (info == null) {
                return "fail";
            } else {
                Map map = new HashMap();

                map.put("userId", info.getId());

                map.put("nickName", info.getNickName());

                String token = JwtUtil.encode(singKey, map, remoteAddr);

                return token;
            }
        }
        return "fail";
    }
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");

        String currentIp = request.getParameter("currentIp");

        Map<String, Object> map = JwtUtil.decode(token, singKey, currentIp);

        if(map!=null){

            String userId = (String)map.get("userId");

            UserInfo userInfo = userInfoService.verify(userId);

            if(userInfo!=null){
                return "success";
            }else{
                return "fail";
            }
        }
        return "fail";
    }
}
