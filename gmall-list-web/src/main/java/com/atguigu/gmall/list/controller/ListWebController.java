package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListWebController {

    @Reference
    ListService listService;
    @Reference
    ManagerService managerService;

    @RequestMapping("list.html")
    public String getSkuLsResult(SkuLsParams skuLsParams, Model model) {
        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.getSearch(skuLsParams);

        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();

        List<BaseAttrInfo> baseAttrInfoList = managerService.getBaseAttrInfo(attrValueIdList);

        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();

        String urlParam = makeUrlParam(skuLsParams);

        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo attrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();

            for (BaseAttrValue baseAttrValue : attrValueList) {

                baseAttrValue.setUrlParam(urlParam);

                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String valueId = skuLsParams.getValueId()[i];
                        if (valueId.equals(baseAttrValue.getId())) {
                            iterator.remove();
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();

                            baseAttrValueSelected.setValueName(attrInfo.getAttrName() + ":" + baseAttrValue.getValueName());

                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);

                            baseAttrValueSelected.setUrlParam(makeUrlParam);

                            baseAttrValuesList.add(baseAttrValueSelected);
                        }
                    }
                }
            }
        }

        int totalPages = (int) ((skuLsResult.getTatal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize());

        model.addAttribute("totalPages", totalPages);

        model.addAttribute("pageNo", skuLsParams.getPageNo());

        model.addAttribute("keyword", skuLsParams.getKeyword());

        model.addAttribute("baseAttrValuesList", baseAttrValuesList);

        model.addAttribute("urlParam", urlParam);

        model.addAttribute("baseAttrInfoList", baseAttrInfoList);

        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        model.addAttribute("skuLsInfoList", skuLsInfoList);

        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParams, String... excludeValueIds) {

        String urlParam = "";

        List<String> paramList = new ArrayList<>();

        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            urlParam += skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            if (urlParam.length() > 0) {
                urlParam += "&";
            }
            urlParam = "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {

            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (excludeValueIds.length > 0 && excludeValueIds != null) {
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)) {
                        continue;
                    }
                }
                if (urlParam.length() > 0) {
                    urlParam += "&";
                }
                urlParam = "valueId=" + valueId;
            }
        }

        return urlParam;
    }
}
