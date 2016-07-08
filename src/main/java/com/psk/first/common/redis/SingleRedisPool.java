package com.psk.first.common.redis;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.psk.first.common.redis.ClusterRedisPool.ExtendsJedisCluster;
import com.psk.first.utils.ConfigUtil;

public class SingleRedisPool implements IRedisType
{

	private String serverName = null;
	private JedisPool redisPool = null;

	public SingleRedisPool(String serverName)
	{
		this.serverName = serverName;
		this.init();
	}

	private void init()
	{
		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		// Maximum active connections to Redis instance
		jedisConfig.setMaxTotal(ConfigUtil.getIntValueFromConfig(getServerName() + ".maxActive", 3000));
		// Number of connections to Redis that just sit there and do nothing
		jedisConfig.setMaxIdle(ConfigUtil.getIntValueFromConfig(getServerName() + ".maxIdle", 300));
		// Minimum number of idle connections to Redis - these can be seen as
		// always open and ready to serve
		jedisConfig.setMinIdle(ConfigUtil.getIntValueFromConfig(getServerName() + ".minIdle", 100));
		String host = ConfigUtil.getStringValueFromConfig(getServerName() + ".domain", "");
		int port = ConfigUtil.getIntValueFromConfig(getServerName() + ".port", 6379);
		int timeout = ConfigUtil.getIntValueFromConfig(getServerName() + ".timeOut", 2000);
		jedisConfig.setMaxWaitMillis(timeout);
		String password = ConfigUtil.getStringValueFromConfig(getServerName() + ".password", "");
		redisPool = new JedisPool(jedisConfig, host, port, timeout, StringUtils.isBlank(password) ? null : password);
	}

	@Override
	public Jedis getRedisClient()
	{
		return redisPool.getResource();
	}

	@Override
	public void returnResource(Jedis j)
	{
		if (null != j && null != this.redisPool)
		{
			this.redisPool.returnResource(j);
		}
	}

	@Override
	public void returnBrokenResource(Jedis j)
	{
		if (null != j && null != this.redisPool)
		{
			this.redisPool.returnBrokenResource(j);
		}
	}

	@Override
	public void destroy()
	{
		this.redisPool.destroy();

	}

	@Override
	public String getServerName()
	{
		return this.serverName;
	}

	@Override
	public ExtendsJedisCluster getRedisClusterClient()
	{
		return null;
	}
}
