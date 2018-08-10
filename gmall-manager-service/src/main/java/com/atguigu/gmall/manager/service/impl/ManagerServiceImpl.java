package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.manager.constant.ManagerConst;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.ManagerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service

public class ManagerServiceImpl implements ManagerService {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getBaseCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();

        baseCatalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();

        baseCatalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();

        baseAttrInfo.setCatalog3Id(catalog3Id);

        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public void insertAttrValue(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            if (baseAttrInfo.getId().length() == 0 || baseAttrInfo.getId() == null) {
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        BaseAttrValue baseAttrValue = new BaseAttrValue();

        baseAttrValue.setAttrId(baseAttrInfo.getId());

        baseAttrValueMapper.delete(baseAttrValue);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue attrValue : attrValueList) {
                if (attrValue.getId() == null || attrValue.getId().length() == 0) {
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue attrValue = new BaseAttrValue();

        attrValue.setAttrId(attrId);

        return baseAttrValueMapper.select(attrValue);
    }

    @Override
    public void delectAttrInfo(String attrId) {
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",attrId);
        baseAttrValueMapper.deleteByExample(example);

        baseAttrInfoMapper.deleteByPrimaryKey(attrId);

    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttr() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public List<SpuInfo> getSpuInfoAttr(String catalog3Id) {
        SpuInfo spuInfoBy3Id = new SpuInfo();

        spuInfoBy3Id.setCatalog3Id(catalog3Id);

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfoBy3Id);

        if(spuInfoList!=null&&spuInfoList.size()>0){
            for (SpuInfo spuInfo : spuInfoList) {
                SpuImage spuImage = new SpuImage();

                spuImage.setSpuId(spuInfo.getId());

                List<SpuImage> imageList = spuImageMapper.select(spuImage);

                spuInfo.setSpuImageList(imageList);

                SpuSaleAttr spuSaleAttr = new SpuSaleAttr();

                spuSaleAttr.setSpuId(spuInfo.getId());

                List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuInfo.getId());

                spuInfo.setSpuSaleAttrList(spuSaleAttrList);



            }
        }
        return spuInfoList;
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        if(spuInfo.getId()==null||spuInfo.getId().length()==0){
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }

        SpuImage spuImage = new SpuImage();

        spuImage.setSpuId(spuInfo.getId());

        spuImageMapper.deleteByPrimaryKey(spuImage);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        for (SpuImage spuImg : spuImageList) {
            if(spuImg.getId()!=null && spuImg.getId().length()==0){
                spuImg.setId(null);
            }
            spuImg.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImg);
        }

        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();

        spuSaleAttr.setSpuId(spuInfo.getId());

        spuSaleAttrMapper.deleteByPrimaryKey(spuSaleAttr);

        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();

        spuSaleAttrValue.setSpuId(spuInfo.getId());

        spuSaleAttrValueMapper.deleteByPrimaryKey(spuSaleAttrValue);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            if(saleAttr.getId()!=null&&saleAttr.getId().length()==0){
                saleAttr.setId(null);
            }
            saleAttr.setSpuId(spuInfo.getId());

            spuSaleAttrMapper.insertSelective(saleAttr);

            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();

            for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                if(saleAttrValue.getId()!=null&&saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                saleAttrValue.setSpuId(spuInfo.getId());

                spuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }
    }

    @Override
    public List<SpuInfo> getSpuInfo(String spuId) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setId(spuId);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        Example example = new Example(SpuImage.class);

        example.createCriteria().andEqualTo("spuId",spuId);

        return spuImageMapper.selectByExample(example);

    }

    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttr(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        if(skuInfo.getId()==null||skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else{
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();

        for (SkuImage image : skuImageList) {
            if(image.getId()!=null&&image.getId().length()==0){
                image.setId(null);
            }
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(image);
        }
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();

        skuSaleAttrValue.setSkuId(skuInfo.getId());

        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        SkuAttrValue skuAttrValue = new SkuAttrValue();

        skuAttrValue.setSkuId(skuInfo.getId());

        skuAttrValueMapper.delete(skuAttrValue);



        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();

        for (SkuAttrValue attrValue : skuAttrValueList) {
            if(attrValue.getId()!=null&&attrValue.getId().length()==0){
                attrValue.setId(null);
            }
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            if(saleAttrValue.getId()!=null&&saleAttrValue.getId().length()==0){
                saleAttrValue.setId(null);
            }
            saleAttrValue.setSkuId(skuInfo.getId());

            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        SkuInfo skuInfo = null;

        String skuInfoKey = ManagerConst.SKUKEY_PREFIX + skuId + ManagerConst.SKUKEY_SUFFIX;

        if(jedis.exists(skuInfoKey)){
            String skuInfoJson = jedis.get(skuInfoKey);
            if(skuInfoJson!=null&& !"".equals(skuInfoJson)){
               skuInfo =  JSON.parseObject(skuInfoJson,SkuInfo.class);
            }
        }else{
           skuInfo =  getSkuInfoMethod(skuId);

           String skuInfoStr = JSON.toJSONString(skuInfo);

           jedis.setex(skuInfoKey,ManagerConst.SKUKEY_TIMEOUT,skuInfoStr);
        }
        jedis.close();
        return skuInfo;
    }

    private SkuInfo getSkuInfoMethod(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);

        SkuImage skuImage = new SkuImage();

        skuImage.setSkuId(skuInfo.getId());

        List<SkuImage> imageList = skuImageMapper.select(skuImage);

        skuInfo.setSkuImageList(imageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        List<SkuAttrValue> attrValueList = skuAttrValueMapper.select(skuAttrValue);

        skuInfo.setSkuAttrValueList(attrValueList);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        List<SkuSaleAttrValue> saleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);

        skuInfo.setSkuSaleAttrValueList(saleAttrValueList);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        List<SpuSaleAttr> spuSaleAttrList =
                 spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()), Long.parseLong(skuInfo.getSpuId()));
        return spuSaleAttrList;
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> saleAttrValueList = skuSaleAttrValueMapper.getSkuSaleAttrValueListBySpu(spuId);
        return saleAttrValueList;
    }

    @Override
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        SkuInfo skuInfoBySpuId = new SkuInfo();
        skuInfoBySpuId.setSpuId(spuId);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfoBySpuId);

        if(skuInfos==null||skuInfos.size()==0){
            return null;
        }else{
            for (SkuInfo skuInfo : skuInfos) {
                SkuImage skuImage = new SkuImage();

                skuImage.setSkuId(skuInfo.getId());

                List<SkuImage> skuImages = skuImageMapper.select(skuImage);

                skuInfo.setSkuImageList(skuImages);

                SkuAttrValue skuAttrValue = new SkuAttrValue();

                skuAttrValue.setSkuId(skuInfo.getId());

                List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);

                skuInfo.setSkuAttrValueList(skuAttrValues);

                SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();

                skuSaleAttrValue.setSkuId(skuInfo.getId());

                List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.select(skuSaleAttrValue);

                skuInfo.setSkuSaleAttrValueList(skuSaleAttrValues);
            }
            return skuInfos;
        }
    }

    @Override
    public List<BaseAttrInfo> getBaseAttrInfo(List<String> attrValueIdList) {
        String attrValueIds = StringUtils.join(attrValueIdList, ",");

        return   baseAttrInfoMapper.getBaseAttrInfoListByValueIdList(attrValueIds);
    }
}
