package com.atguigu.gmall.bean;

import java.io.Serializable;
import java.util.List;

public class SkuLsResult implements Serializable{

    private List<SkuLsInfo> skuLsInfoList;

    private long tatal;

    private long tatalPages;

    private List<String> attrValueIdList;

    public List<SkuLsInfo> getSkuLsInfoList() {
        return skuLsInfoList;
    }

    public void setSkuLsInfoList(List<SkuLsInfo> skuLsInfoList) {
        this.skuLsInfoList = skuLsInfoList;
    }

    public long getTatal() {
        return tatal;
    }

    public void setTatal(long tatal) {
        this.tatal = tatal;
    }

    public long getTatalPages() {
        return tatalPages;
    }

    public void setTatalPages(long tatalPages) {
        this.tatalPages = tatalPages;
    }

    public List<String> getAttrValueIdList() {
        return attrValueIdList;
    }

    public void setAttrValueIdList(List<String> attrValueIdList) {
        this.attrValueIdList = attrValueIdList;
    }
}
