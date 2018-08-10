package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    private static final String INDEX_NAME_GMALL = "gmall";
    private static final String TYPE_NAME_GMALL = "SkuInfo";

    @Autowired
    JestClient jestClient;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        Index index =
                new Index.Builder(skuLsInfo).index(INDEX_NAME_GMALL).type(TYPE_NAME_GMALL).id(skuLsInfo.getId()).build();

        try {
            DocumentResult result = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public SkuLsResult getSearch(SkuLsParams skuLsParams) {

        String query = makeQueryStringForSearch(skuLsParams);

        Search search = new Search.Builder(query).addIndex(INDEX_NAME_GMALL).addType(TYPE_NAME_GMALL).build();

        SearchResult searchResult = null;

        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();

        int timeToes = 10;

        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if (hotScore % timeToes == 0) {
            updateHotScore(skuId, Math.round(hotScore));
        }
    }

    private void updateHotScore(String skuId, long hotScore) {
        String updateJson = "{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":" + hotScore + "\n" +
                "   }\n" +
                "}";
        Update update = new Update.Builder(updateJson).index(INDEX_NAME_GMALL).type(TYPE_NAME_GMALL).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {
        /**讲查询结果封装到这4个属性值上！！！
         * List<SkuLsInfo> skuLsInfoList;

         long tatal;

         long tatalPages;

         List<String> attrValueIdList;
         */
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);

        SkuLsResult skuLsResult = new SkuLsResult();

        List<SkuLsInfo> skuLsInfoList = new ArrayList<>(skuLsParams.getPageSize());
        //循环放入List<SkuLsInfo> skuLsInfoList
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if (hit.highlight != null && hit.highlight.size() > 0) {
                Map<String, List<String>> highlight = hit.highlight;

                List<String> skuNameHighList = highlight.get("skuName");

                String skuNameHigh = skuNameHighList.get(0);

                skuLsInfo.setSkuName(skuNameHigh);
            }
            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);

        //设置总条数
        skuLsResult.setTatal(searchResult.getTotal());

        //设置totalPages 总页码
        long totalPages
                = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTatalPages(totalPages);

        //设置List<String> attrValueIdList;
        List<String> attrValueIdList = new ArrayList<>();

        MetricAggregation aggregations = searchResult.getAggregations();

        TermsAggregation groupby_attr
                = aggregations.getTermsAggregation("groupby_attr");

        if (groupby_attr != null) {
            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();

            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }

        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        //查询对象-->query
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //获取到query下的bool
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //由内到外进行属性的添加 根据skuLsRarams;
        //先添加keyword验空
        if (skuLsParams.getKeyword() != null) {

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());

            boolQuery.must(matchQueryBuilder);
            //高亮显示
            HighlightBuilder highlightBuilder = new HighlightBuilder();

            highlightBuilder.field("skuName");

            highlightBuilder.preTags("<span style='color:red'>");

            highlightBuilder.postTags("</span>");

            sourceBuilder.highlight(highlightBuilder);
        }
        //设置3级分类id
        if (skuLsParams.getCatalog3Id() != null) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());

            boolQuery.filter(termQueryBuilder);
        }
        //设置平台属性id
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String attrValueId = skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", attrValueId);

                boolQuery.filter(termQueryBuilder);
            }
        }

        sourceBuilder.query(boolQuery);
        //分页数据设置
        int form = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();

        sourceBuilder.from(form);

        sourceBuilder.size(skuLsParams.getPageSize());
        //升降序设置
        sourceBuilder.sort("hotScore", SortOrder.DESC);
        //分组查询参数设置
        TermsBuilder groupbyAttr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");

        sourceBuilder.aggregation(groupbyAttr);

        String query = sourceBuilder.toString();

        System.out.println(query.toString());

        return query;
    }
}
