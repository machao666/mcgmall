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

    List<SpuInfo> getSpuInfoAttr(String catalog3Id);
}
