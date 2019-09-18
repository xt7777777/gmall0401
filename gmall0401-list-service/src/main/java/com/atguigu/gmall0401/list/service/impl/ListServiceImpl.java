package com.atguigu.gmall0401.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.SkuLsInfo;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
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

/**
 * @author xtsky
 * @create 2019-09-16 19:45
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;



    @Override
    public void saveSkuLsInfo (SkuLsInfo skuLsInfo){

        Index.Builder builder = new Index.Builder(skuLsInfo);
        builder.index("gmall0401_sku_info").type("doc").id(skuLsInfo.getId());
        Index index = builder.build();
        try {
            DocumentResult documentResult = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParam) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); // 是和写浏览器上的串一样的 (kibana里的命令)

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 商品名称查询搜索
        if (skuLsParam.getKeyword() != null && skuLsParam.getKeyword().length() > 0){
            boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParam.getKeyword()));
            // 高亮
            searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red' >").postTags("</span>"));
        }
        // 三级分类过滤
        else if (skuLsParam.getCatalog3Id() != null && skuLsParam.getCatalog3Id().length() > 0) {
            boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id", skuLsParam.getCatalog3Id()));
        }
        // 平台属性过滤
        if (skuLsParam.getValueId() != null && skuLsParam.getValueId().length > 0) {
            String[] valueIds = skuLsParam.getValueId();
            for (int i = 0; i < valueIds.length; i++) {
                String valueId = valueIds[i];
                boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId", valueId));
            }
        }
        // 价格
//        boolQueryBuilder.filter(new RangeQueryBuilder("price").gte("3200"));
        searchSourceBuilder.query(boolQueryBuilder);

        // 起始行
        searchSourceBuilder.from((skuLsParam.getPageNo()-1)*skuLsParam.getPageSize());

        // 每页行数
        searchSourceBuilder.size(skuLsParam.getPageSize());

        // 聚合
        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_value_id").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);

        // 排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());
        Search search = searchBuilder.addIndex("gmall0401_sku_info").addType("doc").build();
        SkuLsResult skuLsResult = new SkuLsResult();
        try {
            // 商品信息列表
            List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
            SearchResult searchResult = jestClient.execute(search);
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo skuLsInfo = hit.source;

                String skuNameHL = hit.highlight.get("skuName").get(0);
                skuLsInfo.setSkuName(skuNameHL);

                skuLsInfoList.add(skuLsInfo);
            }
            skuLsResult.setSkuLsInfoList(skuLsInfoList);
            skuLsResult.setTotal(searchResult.getTotal());

            // 取记录个数并计算出总页数
            long totalPage = (searchResult.getTotal()+skuLsParam.getPageSize()-1)/skuLsParam.getPageSize();
            skuLsResult.setTotalPages(totalPage);

            // 取出涉及的属性值id
            List<String> attrValueIdList = new ArrayList<>();
            List<TermsAggregation.Entry> groupby_value_id = searchResult.getAggregations().getTermsAggregation("groupby_value_id").getBuckets();
            for (TermsAggregation.Entry bucket : groupby_value_id) {
                String key = bucket.getKey();
                attrValueIdList.add(key);
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();

        // 每次执行在redis中做+1
        // 设计key ->   type key value
        String hotScoreKey = "sku:" + skuId + ":hotscore";
        Long hotScore = jedis.incr(hotScoreKey);
        // 计数可以被10整除时 更新es
        if (hotScore % 10 == 0){
            updateHotScoreEs(skuId,hotScore);
        }

    }


    public void updateHotScoreEs(String skuId, Long hotScore){

        String updateString = "{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+hotScore+"\n" +
                "  }\n" +
                "}";

        Update update = new Update.Builder(updateString).index("gmall0401_sku_info").type("doc").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
