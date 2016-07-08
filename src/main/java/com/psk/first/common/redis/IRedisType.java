package com.psk.first.common.redis;

import redis.clients.jedis.Jedis;

import com.psk.first.common.redis.ClusterRedisPool.ExtendsJedisCluster;

public interface IRedisType
{
	/**
	 * redis的三种部署模式
	 */
	public final static String SINGLE = "single";
	public final static String SENTINEL = "sentinel";
	public final static String CLUSTER = "cluster";

	public Jedis getRedisClient();

	public ExtendsJedisCluster getRedisClusterClient();

	public void returnResource(Jedis j);

	public void returnBrokenResource(Jedis j);

	/**关闭redis连接池 ，redis 将不可用*/
	public void destroy();

	/**
	 * 认证时需要用到
	 * @return
	 */
	public String getServerName();

}
