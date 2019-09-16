package com.atguigu.gmall0401.gmall0401manageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.gmall0401manageservice.mapper.*;
import com.atguigu.gmall0401.service.ManageService;
import com.atguigu.gmall0401.util.RedisUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    public static final String SKUKEY_PREFIX="sku:";
    public static final String SKUKEY_INFO_SUFFIX=":info";
    public static final String SKUKEY_LOCK_SUFFIX=":lock";



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
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        Example example = new Example(BaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
//        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectByExample(example);
//        // 查询平台属性值
//        for (BaseAttrInfo attrInfo : baseAttrInfos) {
//            BaseAttrValue baseAttrValue = new BaseAttrValue();
//            baseAttrValue.setAttrId(attrInfo.getId());
//            List<BaseAttrValue> select = baseAttrValueMapper.select(baseAttrValue);
//            attrInfo.setAttrValueList(select);
//        }

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);

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

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> select = spuImageMapper.select(spuImage);
        return select;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        return spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {



        //保存基本信息
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //保存平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValueList) {
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }

        //保存销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }

        //保存图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage image : skuImageList) {
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(image);
        }

    }

    public SkuInfo getSkuInfoDb(String skuId) {

        System.err.println(Thread.currentThread() + "读取数据库！！");
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            System.err.println("520");
//        }

        //测试 。。
//        Jedis jedis =  redisUtil.getJedis();
//        jedis.set("k1","v21");
//        jedis.close();

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if (skuInfo == null) {
            return null;
        }

        // 图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        // 平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        // 销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        return skuInfo;
    }

    public SkuInfo getSkuInfo_redis(String skuId) {

        SkuInfo skuInfoResult = null;
        int SKU_EXPIRE_SEC=30000;
        // 先查redis 没有再查数据库
        Jedis jedis =  redisUtil.getJedis();
        //redis结构 type  key  value
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUFFIX;
        String skuInfoJson=jedis.get(skuKey);

        if (skuInfoJson != null){

            if (!"EMPTY".equals(skuInfoJson)){
                System.out.println(Thread.currentThread() + "命中缓存！！");
                skuInfoResult = JSON.parseObject(skuInfoJson,SkuInfo.class);
            }

        } else {
            System.out.println(Thread.currentThread() + "未命中缓存！！");
            //setnx 1查锁 exists  2枪锁 set
            //定义一下 锁的结构 type string  key sku:101:lock  value locked
            String lockKey =SKUKEY_PREFIX + skuId + SKUKEY_LOCK_SUFFIX;

//            Long locked = jedis.setnx(lockKey, "locked");
            //锁和时间 不是原子操作 所以合在一起
//            jedis.expire(lockKey,10);
            //设置一个token用来区别是谁的锁
            String token = UUID.randomUUID().toString();
            String locked = jedis.set(lockKey, token, "NX", "EX", 10);
            // 设定过期时间  恩...

            if ("OK".equalsIgnoreCase(locked)){
                System.out.println(Thread.currentThread() + "得到锁！！");
                skuInfoResult = getSkuInfoDb(skuId);

                System.out.println(Thread.currentThread() + "写入缓存！！");
                String skuInfoJsonResult =  null;
                if (skuInfoResult != null){
                    skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                }else {
                    skuInfoJsonResult="EMPTY";
                }
                // System.out.println(Thread.currentThread() + "写入缓存！！");
                // String skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                jedis.setex(skuKey,SKU_EXPIRE_SEC,skuInfoJsonResult);


                System.out.println(Thread.currentThread() + "释放锁！！" + lockKey);

                if (jedis.exists(lockKey) && token.equals(jedis.get(lockKey))){ // 还是有漏洞
                    // 通过后 再要执行删除前 突然锁被换掉 实际删除还是删除别的   非原子化造成的！
                    jedis.del(lockKey); // 只释放自己的锁！ 不能释放别人的
                }

//                jedis.del(lockKey);
            } else {
                System.out.println(Thread.currentThread() + "未得到锁，开始自旋等待！！");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getSkuInfo(skuId);
            }

            //---------------------------------------------------------------
//            System.out.println(Thread.currentThread() + "未命中缓存！！");
//            skuInfoResult = getSkuInfoDb(skuId);
//            System.out.println(Thread.currentThread() + "写入缓存！！");
//            String skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
//            jedis.setex(skuKey,SKU_EXPIRE_SEC,skuInfoJsonResult);
        }

        jedis.close();
        return skuInfoResult;

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfoResult = null;
        int SKU_EXPIRE_SEC=30000;
        // 先查redis 没有再查数据库
        Jedis jedis =  redisUtil.getJedis();
        //redis结构 type  key  value
        String skuKey = SKUKEY_PREFIX + skuId + SKUKEY_INFO_SUFFIX;
        String skuInfoJson=jedis.get(skuKey);

        if (skuInfoJson != null){

            if (!"EMPTY".equals(skuInfoJson)){
                System.out.println(Thread.currentThread() + "命中缓存！！");
                skuInfoResult = JSON.parseObject(skuInfoJson,SkuInfo.class);
            }

        } else {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://redis.gmall.com:6379");

            RedissonClient redissonClient = Redisson.create(config);
            String lockKey = SKUKEY_PREFIX + skuId + SKUKEY_LOCK_SUFFIX;
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = false;

            // lock.lock(10, TimeUnit.SECONDS);
            try {
                locked = lock.tryLock(10, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (locked){
                System.out.println(Thread.currentThread() + "得到锁！！");
                System.out.println(Thread.currentThread() + "再次查询缓存 ！！");
                // 如果得到锁后能够在缓存中查询到 ， 那么直接使用缓存数据 不用再去查询数据库了
                String skuInfoJsonResult = jedis.get(skuKey);
                if (skuInfoJsonResult != null){
                    if (!"EMPTY".equals(skuInfoJsonResult)){
                        System.out.println(Thread.currentThread() + "命中缓存！！");
                        skuInfoResult = JSON.parseObject(skuInfoJsonResult,SkuInfo.class);
                    }
                }else {

                    // 得到锁
//                    System.out.println(Thread.currentThread() + "得到锁！！");
                    skuInfoResult = getSkuInfoDb(skuId);

                    System.out.println(Thread.currentThread() + "写入缓存！！");
//                skuInfoJsonResult = null;
                    if (skuInfoResult != null) {
                        skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                    } else {
                        skuInfoJsonResult = "EMPTY";
                    }
                    // System.out.println(Thread.currentThread() + "写入缓存！！");
                    // String skuInfoJsonResult = JSON.toJSONString(skuInfoResult);
                    jedis.setex(skuKey, SKU_EXPIRE_SEC, skuInfoJsonResult);
                }
                lock.unlock();
            }

        }

        return skuInfoResult;

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String spuId, String skuId) {

        List<SpuSaleAttr> spuSaleAttrListBySpuIdCheckSku = spuSaleAttrMapper.getSpuSaleAttrListBySpuIdCheckSku(spuId, skuId);

        return spuSaleAttrListBySpuIdCheckSku;
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        Map skuValueIdsMap = new HashMap();
        for (Map map : mapList) {
            String skuId = map.get("sku_id") + "";
            String valueIds = map.get("value_ids") + "";
            skuValueIdsMap.put(valueIds,skuId);
        }
        return skuValueIdsMap;
    }
}
