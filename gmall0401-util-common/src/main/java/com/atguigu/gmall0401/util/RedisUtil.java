package com.atguigu.gmall0401.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author xtsky
 * @create 2019-09-14 11:45
 */
public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host, int port, int database) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        // 总数
        jedisPoolConfig.setMaxTotal(200);

        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10 * 1000);

        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);

        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);

        // 在获取连接时，检查是否有效
        jedisPoolConfig.setTestOnBorrow(true);

        // 创建连接池
        jedisPool = new JedisPool(jedisPoolConfig, host, port, 20 * 1000);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
//        jedis.close();
        return jedis;
    }

}
