package com.atguigu.gmall0401.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.BaseAttrInfo;
import com.atguigu.gmall0401.bean.BaseAttrValue;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
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

        // 已选择的平台属性值信息列表
        List<BaseAttrValue> selectedValueList = new ArrayList<>();

        String paramUrl = makeParamUrl(skuLsParams);

        // 把所有已经选择的数值 从属性 属性值 清单中删除属性
        // 清单一 attrList 已选择的属性值
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length >0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();

                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];
                        if (baseAttrValue.getId().equals(selectedValueId)) {
                            iterator.remove(); // 如果清单中的属性值和已选择的属性值相同 那么删除对应的属性行
                            // 添加到已选择列表
                            // baseAttrValue 增加面包屑的url
                            // 历史url - 当前valueId
                            String selectedParamUrl = makeParamUrl(skuLsParams, selectedValueId);// 面包屑的取消路径
                            baseAttrValue.setParamUrl(selectedParamUrl);
                            selectedValueList.add(baseAttrValue);
                        }

                    }
                }

            }
        }

        model.addAttribute("paramUrl", paramUrl); // keyword=xxx&valueId=xxx&valueId=xxx ......

        model.addAttribute("selectedValueList",selectedValueList);

        model.addAttribute("keyword", skuLsParams.getKeyword());

        model.addAttribute("pageNo", skuLsParams.getPageNo());

        model.addAttribute("totalPages", skuLsResult.getTotalPages());

        return "list";

    }


    /**
     * 把页面传入的对象转换为参数url
     * @param skuLsParams
     * @return
     */
    public String makeParamUrl(SkuLsParams skuLsParams, String... excludeValueId){

        String paramUrl="";

        if (skuLsParams.getKeyword() != null){
            paramUrl += "keyword="+skuLsParams.getKeyword();
        } else if (skuLsParams.getCatalog3Id() != null){
            paramUrl += "catalog3Id="+skuLsParams.getCatalog3Id();
        }

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                 String valueId = skuLsParams.getValueId()[i];

                if (excludeValueId != null && excludeValueId.length > 0){
                    // 需要排除的valueid
                    String exValueId = excludeValueId[0];
                    if (valueId.equals(exValueId)){
                        continue;
                    }
                }

                if (paramUrl.length() > 0){
                    paramUrl += "&";
                }
                paramUrl += "valueId="+valueId;
            }
        }

        return paramUrl;

    }

}
