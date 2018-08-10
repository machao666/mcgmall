package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service

public class CartServiceImpl implements CartService {
    @Reference
    ManagerService managerService;
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        /*
        1、	先检查该用户的购物车里是否已经有该商品
        2、	如果有商品，只要把对应商品的数量增加上去就可以，同时更新缓存
        3、	如果没有该商品，则把对应商品插入到购物车中，同时插入缓存。
         */
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);

        CartInfo cartDB = cartInfoMapper.selectOne(cartInfo);

        if (cartDB != null) {
            cartDB.setSkuNum(cartDB.getSkuNum() + skuNum);

            cartInfoMapper.updateByPrimaryKeySelective(cartDB);
        } else {

            SkuInfo skuInfo = managerService.getSkuInfo(skuId);

            CartInfo newCart = new CartInfo();

            newCart.setSkuNum(skuNum);

            newCart.setUserId(userId);

            newCart.setSkuId(skuId);

            newCart.setCartPrice(skuInfo.getPrice());

            newCart.setImgUrl(skuInfo.getSkuDefaultImg());

            newCart.setSkuName(skuInfo.getSkuName());

            newCart.setSkuPrice(skuInfo.getPrice());

            cartInfoMapper.insert(newCart);

            cartDB = newCart;
        }
        Jedis jedis = redisUtil.getJedis();

        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        String cartJson = JSON.toJSONString(cartDB);

        jedis.hset(cartKey, skuId, cartJson);

        String userInfoKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;

        Long ttl = jedis.ttl(userInfoKey);

        jedis.expire(cartKey, ttl.intValue());

        jedis.close();
    }

    /**
     * 1、redis中取出来要进行反序列化
     * 2、redis的hash结构是无序的，要进行排序（可以用时间戳或者主键id，倒序排序）
     * 3、如果redis中没有要从数据库中查询，要连带把最新的价格也取出来，默认要显示最新价格而不是当时放入购物车的价格，如果考虑用户体验可以把两者的差价提示给用户。
     * 4、加载入缓存时一定要设定失效时间，保证和用户信息的失效时间一致即可。
     *
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();

        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        List<String> cartList = jedis.hvals(userCartKey);

        List<CartInfo> cartInfoList = new ArrayList<>();
        if (cartList != null && cartList.size() > 0) {


            for (String cartInfo : cartList) {
                CartInfo cartInfoRd = JSON.parseObject(cartInfo, CartInfo.class);
                System.out.println(cartInfoRd.toString());
                cartInfoList.add(cartInfoRd);
            }

            //排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            System.out.println(cartInfoList.toArray().toString());
            return cartInfoList;
        } else {
            // 从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息。
             cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    //合并cookie与数据库中数据
    @Override
    public List<CartInfo> mergeToList(List<CartInfo> cartListFromCookie, String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        List<CartInfo>  cartCacheList = null;
        if (cartListFromCookie != null && cartListFromCookie.size() > 0) {
            for (CartInfo cartInfoCk : cartListFromCookie) {
                boolean isMatch = false;
                for (CartInfo cartInfoDB : cartInfoList) {
                    if (cartInfoCk.getSkuId().equals(cartInfoDB.getSkuId())) {
                        cartInfoDB.setSkuNum(cartInfoDB.getSkuNum() + cartInfoCk.getSkuNum());

                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);

                        isMatch = true;

                    }
                }
                if (!isMatch) {
                    cartInfoCk.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoCk);
                }
            }
            Jedis jedis = redisUtil.getJedis();

            String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;

            jedis.del(userCheckedKey);
            //合并购物车的时候，勾选状态可能会丢掉，因为DB中没有存储！
            cartCacheList = loadCartCache(userId);
            System.out.println(cartCacheList.toArray().toString());
            for (CartInfo cartInfo : cartCacheList) {
                for (CartInfo info : cartListFromCookie) {
                    if (cartInfo.getSkuId().equals(info.getSkuId())) {
                        if (info.getIsChecked().equals("1")) {
                            cartInfo.setIsChecked(info.getIsChecked());

                            //更新redis中isChecked状态checkCart(cartInfo.getSkuId(), info.getIsChecked(), userId);
                        }
                    }
                }
            }
        }





        return cartCacheList;
    }

    /**
     * 把对应skuId的购物车的信息从redis中取出来，反序列化，修改isChecked标志。
     * 再保存回redis中。
     * 同时保存另一个redis的key 专门用来存储用户选中的商品，方便结算页面使用。
     *
     * @param skuId
     * @param isChecked
     * @param userId
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        Jedis jedis = redisUtil.getJedis();


        String cartJson = jedis.hget(userCartKey, skuId);

        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

        if (cartInfo != null && !"".equals(cartInfo)) {
            cartInfo.setIsChecked(isChecked);

            String cartJsonStr = JSON.toJSONString(cartInfo);

            jedis.hset(userCartKey, skuId, cartJsonStr);

            String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;

            if ("1".equals(isChecked)) {
                jedis.hset(userCheckedKey, skuId, cartJsonStr);
            } else {
                jedis.hdel(userCheckedKey, skuId);
            }
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCheckedCart(String userId) {
        String checkedCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;

        Jedis jedis = redisUtil.getJedis();

        List<String> cartJsonList = jedis.hvals(checkedCartKey);

        List<CartInfo> cartInfoList = new ArrayList<>();
        if (cartJsonList != null && cartJsonList.size() > 0) {

            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

                cartInfoList.add(cartInfo);
            }
            return cartInfoList;
        }
        return null;
    }

    /**
     * 数据库中查找并放入缓存中
     * user：2：cart
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        // select * from cartInfo ,skuInfo c.skuId = s.id where userId = ?
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        if (cartInfoList == null && cartInfoList.size() == 0) {
            return null;
        }
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        Jedis jedis = redisUtil.getJedis();

        Map<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {

            String cartJson = JSON.toJSONString(cartInfo);

            map.put(cartInfo.getSkuId(), cartJson);
        }

        jedis.hmset(userCartKey, map);

        jedis.close();

        return cartInfoList;
    }
}
