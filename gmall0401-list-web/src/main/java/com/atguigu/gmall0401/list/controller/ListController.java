package com.atguigu.gmall0401.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xtsky
 * @create 2019-09-17 18:53
 */
@Controller
@CrossOrigin
public class ListController {

    @Reference
    ListService listService;



    @GetMapping("list.html")
    @ResponseBody
    public String list(SkuLsParams skuLsParams){

        SkuLsResult skuLsInfoList = listService.getSkuLsInfoList(skuLsParams);
        String demo = JSON.toJSONString(skuLsInfoList);
        return demo;

    }

}
