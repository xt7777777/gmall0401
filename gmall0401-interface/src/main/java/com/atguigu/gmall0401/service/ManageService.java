package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.*;

import java.util.List;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-05 22:33
 */
public interface ManageService {

    /**
     * 查询一级分类
     * @return
     */
    public List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级分类id 查询二级分类
     * @param catalog1Id
     * @return
     */
    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级分类id 查询三级分类
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类id 查询平台属性
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存平台属性
     * @param baseAttrInfo
     * @return
     */
    public String saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性id 查询平台属性的详情 顺便把该属性的属性值列表也取到
     * @param attrId
     * @return
     */
    public BaseAttrInfo getBaseAttrInfo(String attrId);

    /**
     * 获得基本销售属性
     * @return
     */
    public List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spu信息
     */
    public void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据三级分类查询spu列表
     */
    public List<SpuInfo> getSpuList(String catalog3Id);


    /**
     * 根据spuid查询spu列表
     * @param spuId
     * @return
     */
    public List<SpuImage> getSpuImageList(String spuId);

    /**
     * 根据spuid查询销售属性
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 保存skuInfo
     */
    public void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 查询skuInfo
     */
    public SkuInfo getSkuInfo(String skuId);

    /**
     * 根据spuid查询销售属性，选中传入的sku涉及的销售属性
     */
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String spuId,String skuId);

    /**
     * 根据spuid查询已有的sku涉及的销售属性清单
     */
    public Map getSkuValueIdsMap(String spuId);



}
