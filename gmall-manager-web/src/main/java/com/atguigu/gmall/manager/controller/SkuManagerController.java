package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManagerService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuManagerController {

    @Reference
    ManagerService managerService;

    @ResponseBody
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        List<SpuSaleAttr> spuSaleAttrList = managerService.getSpuSaleAttr(spuId);
        return spuSaleAttrList;
    }
    @ResponseBody
    @RequestMapping(value = "saveSkuInfo" ,method = RequestMethod.POST)
    public void saveSkuInfo(SkuInfo skuInfo){

        managerService.saveSkuInfo(skuInfo);
    }
}
