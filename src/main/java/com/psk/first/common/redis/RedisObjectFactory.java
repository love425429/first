package com.psk.first.common.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.psk.first.common.LogUtil;
import com.psk.first.utils.ConfigUtil;

public class RedisObjectFactory
{
	private final Map<String, IRedisType> redisObjectSet = Collections
			.synchronizedMap(new HashMap<String, IRedisType>());

	private final static class RedisFacSingleton
	{
		private final static RedisObjectFactory instance = new RedisObjectFactory();
	}

	private RedisObjectFactory()
	{

	}

	public static final RedisObjectFactory getInstance()
	{
		return RedisFacSingleton.instance;
	}

	private final static Log log = LogFactory.getLog(RedisObjectFactory.class);

	public synchronized IRedisType build(final String redisServerName)
	{
		if (StringUtils.isBlank(redisServerName))
		{
			LogUtil.INFO(log, "RedisObjectFactory", "build", "checkRedisServerName", "error", "redisServerName",
					redisServerName);
			return null;
		}
		if (redisObjectSet.containsKey(redisServerName) && redisObjectSet.get(redisServerName) != null)
		{
			return redisObjectSet.get(redisServerName);
		}
		String redisType = ConfigUtil.getStringValueFromConfig(redisServerName + ".workPattern", "");
		if (StringUtils.isBlank(redisType))
		{
			LogUtil.INFO(log, "RedisObjectFactory", "build", "checkRedisType", "error", "redisType", redisType);
			return null;
		}
		IRedisType redis = null;
		if (redisType.equalsIgnoreCase(IRedisType.SINGLE))
		{
			redis = buildSingle(redisServerName);
		}
		else if (redisType.equalsIgnoreCase(IRedisType.SENTINEL))
		{
			redis = buildSentinel(redisServerName);
		}
		else if (redisType.equalsIgnoreCase(IRedisType.CLUSTER))
		{
			redis = buildCluster(redisServerName);
		}
		if (redis != null)
		{
			redisObjectSet.put(redisServerName, redis);
			return redis;
		}
		LogUtil.INFO(log, "RedisObjectFactory", "build", "buildReturn", "error", "redisServerName", redisServerName,
				"redisType", redisType, "IRedisType", null);
		return null;
	}

	private IRedisType buildSingle(final String redisServerName)
	{
		return new SingleRedisPool(redisServerName);
	}

	private IRedisType buildSentinel(final String redisServerName)
	{
		return new SentinelRedisPool(redisServerName);
	}

	private IRedisType buildCluster(final String redisServerName)
	{
		return new ClusterRedisPool(redisServerName);
	}

	@PreDestroy
	public void destory()
	{
		if (redisObjectSet == null || redisObjectSet.size() == 0)
		{
			return;
		}
		Iterator<IRedisType> it = redisObjectSet.values().iterator();
		while (it.hasNext())
		{
			IRedisType i = it.next();
			try
			{
				i.destroy();
			}
			catch (Exception e)
			{

			}
		}
	}
}
