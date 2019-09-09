package com.atguigu.gmall0401.gmall0401manageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.SkuInfo;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xtsky
 * @create 2019-09-09 19:22
 */
@RestController
public class SkuController {


    @Reference
    ManageService manageService;


    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){

        manageService.saveSkuInfo(skuInfo);

        return "success";

    }

}
