package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo>{
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    List<BaseAttrInfo> getBaseAttrInfoListByValueIdList(@Param("attrValues") String attrValueIds);
}
