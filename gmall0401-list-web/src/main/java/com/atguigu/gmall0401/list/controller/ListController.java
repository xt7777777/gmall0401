package com.atguigu.gmall0401.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.BaseAttrInfo;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-17 18:53
 */
@Controller
@CrossOrigin
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;



    @GetMapping("list.html")
    //@ResponseBody
    public String list(SkuLsParams skuLsParams, Model model){

        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);

        model.addAttribute("skuLsResult", skuLsResult);
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        model.addAttribute("attrList", attrList);

        return "list";

    }

}
