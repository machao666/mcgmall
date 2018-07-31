package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManagerService;
import groovy.util.logging.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrManagerController {

    @Reference
    ManagerService managerService;

    @RequestMapping("/getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1() {
        return managerService.getBaseCatalog1();
    }

    @RequestMapping("/getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        return managerService.getBaseCatalog2(catalog1Id);
    }

    @RequestMapping("/getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        return managerService.getBaseCatalog3(catalog2Id);
    }

    @RequestMapping("/getAttrInfo")
    @ResponseBody
    public List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id) {
        return managerService.attrInfoList(catalog3Id);
    }

    @RequestMapping("/saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        managerService.insertAttrValue(baseAttrInfo);
        return "success";
    }

    @RequestMapping("/getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        return managerService.getAttrValueList(attrId);
    }

    @RequestMapping("/delectAttrInfo")
    @ResponseBody
    public String delectAttrInfo(String attrId){
        managerService.delectAttrInfo(attrId);
        return "success";
    }
}
