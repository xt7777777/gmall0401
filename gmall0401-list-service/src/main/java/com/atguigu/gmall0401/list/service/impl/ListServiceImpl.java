package com.atguigu.gmall0401.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.SkuLsInfo;
import com.atguigu.gmall0401.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
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


}
