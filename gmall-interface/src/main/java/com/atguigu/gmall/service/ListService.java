package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

public interface ListService {

    void saveSkuInfo(SkuLsInfo skuLsInfo);

    SkuLsResult getSearch(SkuLsParams skuLsParams);

    void incrHotScore(String skuId);
}
