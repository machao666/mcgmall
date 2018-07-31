package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManagerService {

    List<BaseCatalog1> getBaseCatalog1();

    List<BaseCatalog2> getBaseCatalog2(String catalog1Id);

    List<BaseCatalog3> getBaseCatalog3(String catalog2Id);

    List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id);

    void insertAttrValue(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);

    void delectAttrInfo(String attrId);

    List<BaseSaleAttr> getBaseSaleAttr();

    List<SpuInfo> getSpuInfoAttr(String catalog3Id);

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuInfo> getSpuInfo(String spuId);

    List<SpuImage> getSpuImageList(String spuId);


    List<BaseAttrInfo> attrInfoList(String catalog3Id);

    List<SpuSaleAttr> getSpuSaleAttr(String spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
