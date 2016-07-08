package com.psk.first.common.redis;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import com.psk.first.common.redis.ClusterRedisPool.ExtendsJedisCluster;
import com.psk.first.utils.ConfigUtil;

public class SentinelRedisPool implements IRedisType
{
	private String serverName = null;
	private JedisSentinelPool redisPool = null;

	public SentinelRedisPool(String serverName)
	{
		this.serverName = serverName;
		this.init();
	}

	private void init()
	{
		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		// Maximum active connections to Redis instance
		jedisConfig.setMaxTotal(ConfigUtil.getIntValueFromConfig(getServerName() + ".maxActive", 600));
		// Number of connections to Redis that just sit there and do nothing
		jedisConfig.setMaxIdle(ConfigUtil.getIntValueFromConfig(getServerName() + ".maxIdle", 300));
		// Minimum number of idle connections to Redis - these can be seen as
		// always open and ready to serve
		jedisConfig.setMinIdle(ConfigUtil.getIntValueFromConfig(getServerName() + ".minIdle", 100));
		/**sentinel服务地址*/
		String masterName = ConfigUtil.getStringValueFromConfig(getServerName() + ".masterName", "master");
		//${host1}:26379,${host2}:26379
		String sentinelStr = ConfigUtil.getStringValueFromConfig(getServerName() + ".sentinels", null);
		String[] sentinelArr = sentinelStr.split("\\,");
		final Set<String> sentinels = new HashSet<String>();
		if (null == sentinelArr)
		{
			sentinels.add("127.0.0.1:26379");
			sentinels.add("127.0.0.1:26380");
		}
		else
		{
			for (int i = 0; i < sentinelArr.length; i++)
			{
				sentinels.add(sentinelArr[i]);
			}
		}
		int timeout = ConfigUtil.getIntValueFromConfig(getServerName() + ".timeOut", 6000);
		jedisConfig.setMaxWaitMillis(timeout);
		jedisConfig.setTestOnBorrow(true);
		jedisConfig.setTestOnReturn(true);
		String password = ConfigUtil.getStringValueFromConfig(getServerName() + ".password", "");
		redisPool = new JedisSentinelPool(masterName, sentinels, jedisConfig, timeout,
				StringUtils.isBlank(password) ? null : password);
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
