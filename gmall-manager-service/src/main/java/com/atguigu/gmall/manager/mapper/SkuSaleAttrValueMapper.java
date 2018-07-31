package com.atguigu.gmall.manager.mapper;


import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public  interface  SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue>{
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
