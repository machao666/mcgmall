package com.atguigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.annotation.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartInfoController {

    @Reference
    CartService cartService;
    @Autowired
    CartCookieHandler cartCookieHandler;
    @Reference
    ManagerService manageService;


    @RequestMapping(value = "addToCart", method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {
        /**
         * 1、	获得参数：skuId 、skuNum
         2、	判断该用户是否登录，用userId判断
         3、	如果登录则调用后台的service的业务方法
         4、	如果未登录，要把购物车信息暂存到cookie中。
         5、	实现利用cookie保存购物车的方法。
         */

        String skuId = request.getParameter("skuId");

        String skuNum = request.getParameter("skuNum");

        String userId = (String) request.getAttribute("userId");

        if (userId != null && userId.length() > 0) {
            cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        } else {
            cartCookieHandler.addToCart(request, response, skuId, userId, Integer.parseInt(skuNum));
        }
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);

        return "success";
    }

    /**
     * 1、	展示购物中的信息
     * 2、	如果用户已登录从缓存中取值，如果缓存没有，加载数据库。
     * 3、	如果用户未登录从cookie中取值。
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response) {

        String userId = (String) request.getAttribute("userId");

        if (userId != null) {

            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);

            List<CartInfo> cartInfoList = null;

            if (cartListFromCookie != null && cartListFromCookie.size() > 0) {

                //开始合并
                cartInfoList = cartService.mergeToList(cartListFromCookie, userId);

                // 取得ischecked 集合 ，cartInfoList 水库Id==.cartInfoList ,ck cartinfo.setIscheck=ck.ischeckecd
                // 删除cookie中数据
                cartCookieHandler.deleteCartCookie(request, response);

            }else{
                cartInfoList = cartService.getCartList(userId);
                System.out.println(cartInfoList.toArray().toString());
            }
            request.setAttribute("cartInfoList", cartInfoList);
        } else {
            List<CartInfo> cartInfoList = cartCookieHandler.getCartList(request);

            request.setAttribute("cartInfoList", cartInfoList);
        }

        return "cartList";
    }

    /**
     * 同样这里要区分，用户登录和未登录状态。
     * 如果登录，修改缓存中的数据，如果未登录，修改cookie中的数据。
     *
     * @param request
     * @param response
     *
     */
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request, HttpServletResponse response) {
        String skuId = request.getParameter("skuId");

        String isChecked = (String) request.getParameter("isChecked");

        String userId = (String) request.getAttribute("userId");

        if (userId != null) {
            cartService.checkCart(skuId, isChecked, userId);
        } else {
            cartCookieHandler.checkCart(request, response, skuId, isChecked);
        }
    }

    /**
     * 要解决用户在未登录且购物车中有商品的情况下，直接点击结算。
     * 所以不能直接跳到结算页面，要让用户强制登录后，检查cookie并进行合并后再重定向到结算页面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartList = cartCookieHandler.getCartList(request);

        if (cartList != null && cartList.size() > 0) {
            List<CartInfo> cartInfoList = cartService.mergeToList(cartList,userId);

            cartCookieHandler.deleteCartCookie(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }
}
