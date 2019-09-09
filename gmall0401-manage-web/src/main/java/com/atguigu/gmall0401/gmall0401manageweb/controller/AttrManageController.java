package com.atguigu.gmall0401.gmall0401manageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-05 22:46
 */
@CrossOrigin
@RestController
public class AttrManageController {

    @Reference
    private ManageService manageService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(@RequestParam("catalog3Id") String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        String s = manageService.saveBaseAttrInfo(baseAttrInfo);

        return s;

    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){

        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);

        return baseAttrInfo.getAttrValueList();

    }


    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }


    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){

        List<BaseSaleAttr> baseSaleAttrs = manageService.getBaseSaleAttrList();
        return baseSaleAttrs;

    }

    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id) {

        return manageService.getSpuList(catalog3Id);

    }

}
