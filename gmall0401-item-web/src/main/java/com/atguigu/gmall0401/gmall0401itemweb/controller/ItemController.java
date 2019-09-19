package com.atguigu.gmall0401.gmall0401itemweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.SkuInfo;
import com.atguigu.gmall0401.bean.SpuSaleAttr;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.lang.model.element.VariableElement;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-11 18:15
 */
@Controller
@CrossOrigin
public class ItemController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;



    @GetMapping("{skuId}.html")
    @LoginRequire
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request){

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        List<SpuSaleAttr> spuSaleAttrListCheckSku = manageService.getSpuSaleAttrListCheckSku(skuInfo.getSpuId(), skuId);

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrListCheckSku);

        // 得到属性组合与skuid的映射关系 ， 用于页面 根据属性组合进行跳转
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("valuesSkuJson",valuesSkuJson);
//        String skuInfoJson = JSON.toJSONString(skuInfo);
//        return skuInfoJson;

        listService.incrHotScore(skuId);

        Object userId = request.getAttribute("userId");
        System.err.println("userId = " + userId);
        return "item";

    }

}
