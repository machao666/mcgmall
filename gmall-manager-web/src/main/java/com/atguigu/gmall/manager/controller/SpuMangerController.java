package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.ManagerService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class SpuMangerController {

    @Value("${fileServer.url}")
    private String fileUrl;
    @Reference
    ManagerService managerService;

    @RequestMapping("spuListPage")
    public String spuListPage() {
        return "spuListPage";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id) {
        return managerService.getSpuInfoAttr(catalog3Id);
    }

    @RequestMapping(value = "fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String imgUrl = fileUrl;
        if (file != null) {
            System.out.println("multipartFile = " + file.getName() + "|" + file.getSize());

            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);

            String fileName = file.getOriginalFilename();

            String extName = StringUtils.substringAfterLast(fileName, ".");

            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);


            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl+="/"+path;
            }
        }
        return imgUrl;
    }
    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public  List<BaseSaleAttr> getBaseSaleAttr(){
        return managerService.getBaseSaleAttr();
    }

    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        managerService.saveSpuInfo(spuInfo);
        return "success";
    }

    @RequestMapping(value = "getSpuInfo")
    @ResponseBody
    public List<SpuInfo> getSpuInfo(String spuId){
        return managerService.getSpuInfo(spuId);
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> getSpuImageList(String spuId){
        return managerService.getSpuImageList(spuId);
    }


}

