package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.util.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;

    @Reference
    ManagerService managerService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum) {
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();

        boolean ifExist = false;
        if (cookieValue != null) {
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);

                    cartInfo.setSkuPrice(cartInfo.getCartPrice());

                    ifExist = true;

                    break;
                }
            }
        }
        if (!ifExist) {
            SkuInfo skuInfo = managerService.getSkuInfo(skuId);

            CartInfo cartInfo = new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);

            cartInfoList.add(cartInfo);
        }
        String newCartJson = JSON.toJSONString(cartInfoList);

        CookieUtil.setCookie(request, response, cookieCartName, newCartJson, COOKIE_CART_MAXAGE, true);
    }

    /**
     * 查询cookie 中购物车列表
     *
     * @param request
     * @param userId
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request, response, cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);

        if (cartInfoList != null && cartInfoList.size() > 0) {
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }
        String cartStr = JSON.toJSONString(cartInfoList);

        CookieUtil.setCookie(request,response,cookieCartName,cartStr,COOKIE_CART_MAXAGE,true);
    }
}
