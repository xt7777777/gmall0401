package com.atguigu.gmall0401.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.SkuLsInfo;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
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

import java.io.IOException;

/**
 * @author xtsky
 * @create 2019-09-16 19:45
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

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
        boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParam.getKeyword()));
        // 三级分类过滤
        boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParam.getCatalog3Id()));
        // 平台属性过滤
        String[] valueIds = skuLsParam.getValueId();
        for (int i = 0; i < valueIds.length; i++) {
            String valueId = valueIds[i];
            boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId",valueId));
        }
        // 价格
//        boolQueryBuilder.filter(new RangeQueryBuilder("price").gte("3200"));
        searchSourceBuilder.query(boolQueryBuilder);

        // 起始行
        searchSourceBuilder.from((skuLsParam.getPageNo()-1)*skuLsParam.getPageSize());

        // 每页行数
        searchSourceBuilder.size(skuLsParam.getPageSize());

        // 高亮
        searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red' >").postTags("</span>"));

        // 聚合
        TermsBuilder aggsBuilder = AggregationBuilders.terms("groupby_value_id").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);

        // 排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        Search.Builder searchBuilder = new Search.Builder(searchSourceBuilder.toString());
        Search search = searchBuilder.addIndex("gmall0401_sku_info").addType("doc").build();
        try {
            SearchResult execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
