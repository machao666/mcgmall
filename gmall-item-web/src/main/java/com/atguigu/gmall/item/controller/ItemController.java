package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.util.annotation.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    ListService listService;
    @Reference
    ManagerService managerService;


    @RequestMapping("{skuId}.html")

    public String index(@PathVariable(value = "skuId")String skuId, HttpServletRequest request){

        SkuInfo skuInfo = managerService.getSkuInfo(skuId);

        List<SpuSaleAttr> spuSaleAttrList = managerService.selectSpuSaleAttrListCheckBySku(skuInfo);

        List<SkuSaleAttrValue> saleAttrValueList = managerService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        String skuJson = "";

        HashMap<String , String> skuMap = new HashMap<>();
        for (int i = 0; i < saleAttrValueList.size(); i++) {

            if(skuJson.length()!=0){
                skuJson +="|";
            }

            skuJson += saleAttrValueList.get(i).getSaleAttrValueId();

            if((i+1)==saleAttrValueList.size()||!saleAttrValueList.get(i).getSkuId().equals(saleAttrValueList.get(i+1).getSkuId())) {
                skuMap.put(skuJson,saleAttrValueList.get(i).getSkuId());

                skuJson = "";
            }
        }

        listService.incrHotScore(skuId);

        String skuJsonString = JSON.toJSONString(skuMap);

        request.setAttribute("skuJsonString",skuJsonString);

        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        request.setAttribute("skuInfo",skuInfo);

        return "index";
    }
}
