package com.atguigu.gmall0401.gmall0401manageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.gmall0401manageservice.mapper.*;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-05 22:35
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;




    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        Example example = new Example(BaseAttrInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectByExample(example);
        return baseAttrInfos;
    }

    @Override
    @Transactional
    public String saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {

        if (baseAttrInfo.getId() != null || baseAttrInfo.getId().length() > 0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);//根据attrid先全部删除 在重新保存新的 实现修改功能

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        int i1 = 0;
        for (BaseAttrValue baseAttrValue : attrValueList) {
            String id = baseAttrInfo.getId();
            baseAttrValue.setAttrId(id);
            i1 = baseAttrValueMapper.insertSelective(baseAttrValue);
        }
        return i1 > 0 ? "success" : "error";
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectByExample(example);

        baseAttrInfo.setAttrValueList(baseAttrValues);
        return baseAttrInfo;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();
        return baseSaleAttrs;
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //spu基本信息
        spuInfoMapper.insertSelective(spuInfo);
        //图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
        //销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            //销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }
}
