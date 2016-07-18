package com.psk.first.common.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import com.psk.first.common.LogUtil;
import com.psk.first.utils.JacksonUtils;
import com.psk.first.utils.SerializeUtil;

public class RedisUtil implements IRedisIOp
{
	private final static Log log = LogFactory.getLog(RedisUtil.class);

	private IRedisType redisPool = null;

	public RedisUtil(String serverName)
	{
		RedisObjectFactory redisObjectFactory = RedisObjectFactory.getInstance();
		this.redisPool = redisObjectFactory.build(serverName);
	}

	@Override
	public String javaSet(String key, Object value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = this.redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.javaSerialize(value);
			return jedis.set(keyBytes, valueBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "javaSet", "set", null);
			this.redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			///	... cluster pattern not to do so
			this.redisPool.returnResource(jedis);
		}
	}

	@Override
	public String javaSetex(String key, int seconds, Object value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.javaSerialize(value);
			return jedis.setex(keyBytes, seconds, valueBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "javaSetex", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			redisPool.returnResource(jedis);
		}
	}

	@Override
	public Object javaGet(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = this.redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = jedis.get(keyBytes);
			if (null == valueBytes)
			{
				LogUtil.INFO(log, "RedisUtil", "javaGet", "valueBytes", "error", "key", key, "valueBytes", null);
				return null;
			}
			LogUtil.INFO(log, "RedisUtil", "javaGet", "hit", "info", "key", key);
			return SerializeUtil.javaDeserialize(valueBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "javaGet", "exception", null);
			this.redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				this.redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public String hessianSet(String key, Object value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.hessian2Serialize(value);
			String result = jedis.set(keyBytes, valueBytes);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianSet", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public String hessianSetex(String key, int seconds, Object value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.hessian2Serialize(value);
			String result = jedis.setex(keyBytes, seconds, valueBytes);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianSetex", "exception", "key", key, "seconds", seconds);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	//TODO --->>>
	@Override
	public Object hessianGet(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = jedis.get(keyBytes);
			if (null == valueBytes)
			{
				//log.cacheInfo("Get Key：" + key + " missed!");
				return null;
			}
			//log.cacheInfo("Get Key：" + key + " hit!");
			return SerializeUtil.hessian2Deserialize(valueBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianGet", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		catch (Throwable e)
		{
			LogUtil.ERROR(log, null, "RedisUtil", "hessianGet", "Throwable", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 模糊查询所需的keys pattern命令
	 * @param pattern
	 * @return
	 */
	@Override
	public Set<String> getKeySetByPattern(String pattern)
	{
		if (null == pattern)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Set<String> setKeys = jedis.keys(pattern);
			return setKeys;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "getKeySetByPattern", "exception", "pattern", pattern);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 模糊查询：需要保证pattern查询得到的value类型一致，并在外部对模糊查询结果进行合并
	 * @param pattern
	 * @return
	 */
	@Override
	public List<Object> patternHessianGet(String pattern)
	{
		if (null == pattern)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		String keysInKeySet = null;
		try
		{
			Set<String> setKeys = jedis.keys(pattern);
			if (null == setKeys || setKeys.isEmpty())
			{
				//log.infoInfo("patternHessianGet not find keypattern:" + pattern);
				return null;
			}
			Object valueInKeysSet = null;
			Iterator<String> it = setKeys.iterator();
			List<Object> objectList = new ArrayList<Object>();
			while (it.hasNext())
			{
				keysInKeySet = it.next();
				byte[] keyBytes = keysInKeySet.getBytes("UTF-8");
				byte[] valueBytes = jedis.get(keyBytes);
				valueInKeysSet = SerializeUtil.hessian2Deserialize(valueBytes);
				//				log.cacheInfo(keysInKeySet + "=" + valueInKeysSet);
				objectList.add(valueInKeysSet);
			}
			return objectList;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "patternHessianGet", "exception", "pattern", pattern);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		catch (Throwable e)
		{
			LogUtil.ERROR(log, null, "RedisUtil", "patternHessianGet", "Throwable", "pattern", pattern);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public boolean isKeyExist(String key)
	{
		if (null == key)
		{
			return false;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			return jedis.exists(keyBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "isKeyExist", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;//异常后暂时视为不存在
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public void hessianDel(String key)
	{
		if (null == key)
		{
			return;
		}
		Jedis jedis = redisPool.getRedisClient();

		{

		}
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			Long ret = jedis.del(keyBytes);
			return;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianDel", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	@Override
	public Object delKey(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.del(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "delKey", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long delKeys(String[] keys)
	{
		if (null == keys)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.del(keys);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "delKeys", "exception", "keys", keys);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * zset有序集合
	 * @param key
	 * @return
	 */
	@Override
	public Set<String> zsetMembers(String key)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Set<String> members = jedis.zrange(key, 0, -1);
			return members;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zsetMembers", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * zadd
	 * @param key
	 * @param score
	 * @return
	 */
	@Override
	public Long zadd(String key, double score, String value)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zadd(key, score, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zadd", "exception", "key", key, "value", value, "score", score);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * zrem
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public void zrem(String key, String value)
	{
		if (StringUtils.isBlank(key))
			return;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long ret = jedis.zrem(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrem", "exception", "key", key, "value", value);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 需确认是根据权重的升序还是降序（此处按照升序）
	 * @param key
	 * @return
	 */
	@Override
	public Long zcard(String key)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zcard(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zcard", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long zcount(String key, double min, double max)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zcount(key, min, max);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zcount", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	@Override
	public Set<String> zrange(String key, long start, long end)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrange(key, start, end);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrange", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrangeWithScores(key, start, end);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrangeWithScores", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrangeByScore(key, min, max);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrangeByScore", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrevrange(key, start, end);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrevrange", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrevrangeWithScores(key, start, end);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrevrangeWithScores", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 删除有序集合中start到end之间的元素
	 * @param key
	 * @param start
	 * @return
	 */
	@Override
	public void zcut(String key, int start, int end)
	{
		if (StringUtils.isBlank(key) || start < 0)
			return;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long ret = jedis.zremrangeByRank(key, start, end);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zcut", "exception", "key", key, "start", start, "end", end);
			redisPool.returnBrokenResource(jedis);
			return;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 获取有序集合中member对应的score
	 * @param key
	 * @param member
	 * @return
	 */
	@Override
	public Double zscore(String key, String member)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zscore(key, member);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zscore", "exception", "key", key, "member", member);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 删除一定排名范围内的元素
	 * @param key
	 * @return
	 */
	@Override
	public Long zremrangebyrank(String key, long min, long max)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zremrangeByRank(key, min, max);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zremrangebyrank", "exception", "key", key, "min", min, "max", max);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long zrank(String key, String member)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrank(key, member);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrank", "exception", "key", key, "member", member);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long zrevrank(String key, String member)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrevrank(key, member);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrevrank", "exception", "key", key, "member", member);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public String hget(String key, String field)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hget(key, field);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hget", "exception", "key", key, "field", field);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long hset(String key, String field, String value)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hset(key, field, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hset", "exception", "key", key, "field", field, "value", value);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public boolean hexists(String key, String field)
	{
		if (StringUtils.isBlank(key))
		{
			return false;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hexists(key, field);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hexists", "exception", "key", key, "field", field);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Map<String, String> hgetAll(String key)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hgetAll(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hgetAll", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Set<String> hkeys(String key)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hkeys(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hkeys", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public List<String> hvals(String key)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hvals(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hvals", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public List<String> hmget(String key, String[] fields)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hmget(key, fields);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hmget", "exception", "key", key, "fields", fields);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public void hmset(String key, Map<String, String> hash)
	{
		if (StringUtils.isBlank(key))
		{
			return;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String ret = jedis.hmset(key, hash);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hmset", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long hdel(String key, String... fields)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.hdel(key, fields);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hdel", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public Set<String> smembers(String key)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.smembers(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "smembers", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public long scard(String key)
	{
		if (StringUtils.isBlank(key))
			return 0;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.scard(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "scard", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return 0;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long sadd(String key, String[] value)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.sadd(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "sadd", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void srem(String key, String[] value)
	{
		if (StringUtils.isBlank(key))
			return;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long ret = jedis.srem(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "srem", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 返回集合中的一个随机元素。
	 * @param key
	 * @param value
	 * @return 
	 */
	@Override
	public String srandMember(String key)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.srandmember(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "srandMember", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 返回集合中的多个随机元素。如果 count 大于等于集合基数，那么返回整个集合。
	 * @param key
	 * @param value
	 * @return 
	 */
	@Override
	public List<String> srandMember(String key, int count)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.srandmember(key, count);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "srandMember", "exception", "key", key, "count", count);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 移除并返回set集合中的一个随机元素。当 key 不存在或 key 是空集时，返回 nil 。
	 * @param key
	 * @param value
	 * @return 
	 */
	@Override
	public String spop(String key)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.spop(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "spop", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。原子操作
	 * 如果 source 集合不存在或不包含指定的 member 元素，则 SMOVE 命令不执行任何操作，仅返回 0 
	 * @param key
	 * @param value
	 * @return 
	 */
	@Override
	public Long smove(String srcKey, String dstKey, String member)
	{
		if (StringUtils.isBlank(srcKey))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.smove(srcKey, dstKey, member);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "smove", "exception", "srcKey", srcKey, "dstKey", dstKey);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public Long sadd(String key, String value)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.sadd(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "sadd", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void srem(String key, String value)
	{
		if (StringUtils.isBlank(key))
			return;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long ret = jedis.srem(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "srem", "exception", "key", key, "value", value);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public void setExpireTime(String key, int seconds)
	{
		if (key == null)
			return;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long ret = jedis.expire(key, seconds);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "setExpireTime", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public void setExpireAt(String key, long unixTime)
	{
		if (key == null)
			return;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long ret = jedis.expireAt(key, unixTime);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "setExpireAt", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Set<String> keys(String pattern)
	{
		if (null == pattern)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.keys(pattern);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "keys", "exception", "pattern", pattern);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public String set(String key, String value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.set(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "set", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	@Override
	public String setex(String key, int seconds, String value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.setex(key, seconds, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "setex", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	@Override
	public Long setnx(String key, String value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long result = jedis.setnx(key, value);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "setnx", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	@Override
	public Object get(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String value = jedis.get(key);
			if (null == value)
			{
				return null;
			}
			return value;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "get", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Object rpop(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String value = jedis.rpop(key);
			if (null == value)
			{
				return null;
			}
			return value;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "rpop", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Object lpop(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String value = jedis.lpop(key);
			if (null == value)
			{
				return null;
			}
			return value;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "lpop", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public List<String> getRange(String key, int begin, int end)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			List<String> values = jedis.lrange(key, begin, end);
			if (null == values || values.size() < 1)
			{
				return null;
			}
			return values;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "getRange", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long lpush(String key, String value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.lpush(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "lpush", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	/**
	 * mset相当于 jedis.set("name","minxr"); jedis.set("jarorwar","aaa");  
	        jedis.mset("name", "minxr", "jarorwar", "aaa");  
	        System.out.println(jedis.mget("name", "jarorwar"));  
	 * @param keysvalues
	 * @return
	 */
	@Override
	public String mset(String[] keysvalues)
	{
		if (null == keysvalues)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.mset(keysvalues);//(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "mset", "exception", "keysvalues", keysvalues);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public String mset(byte[][] keysvalues)
	{
		if (null == keysvalues)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.mset(keysvalues);//(key, value);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "mset", "exception", "keysvalues", keysvalues);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public List<String> mget(String[] keys)
	{
		if (null == keys)
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			List<String> values = jedis.mget(keys);
			if (null == values)
			{
				return null;
			}
			return values;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "mget", "exception", "keys", keys);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			/// ... it's important to return the Jedis instance to the pool once you've finished using it
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long incr(String key)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long result = jedis.incr(key);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "incr", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	public Long incrBy(String key, long value)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Long result = jedis.incrBy(key, value);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "incrBy", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
		return null;
	}

	/**
	 * 对sorted set的score加1
	 * @param key
	 * @param member
	 * @return
	 */
	@Override
	public Double zincr(String key, String member)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Double result = jedis.zincrby(key, 1, member);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zincr", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
		return null;
	}

	@Override
	public Double zincrBy(String key, double score, String member)
	{
		if (StringUtils.isBlank(key))
			return null;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Double result = jedis.zincrby(key, score, member);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zincrBy", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
		return null;
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 */
	@Override
	public void hessianHset(String key, String field, Object value)
	{
		if (StringUtils.isBlank(key))
		{
			return;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] fieldBytes = field.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.hessian2Serialize(value);
			Long ret = jedis.hset(keyBytes, fieldBytes, valueBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianHset", "exception", "key", key, "field", field);
			redisPool.returnBrokenResource(jedis);
			return;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 获取hash中的所有对象
	 * @param key
	 * @return
	 */
	@Override
	public <E> List<E> hessianHgetAll(String key)
	{
		List<E> ret = new ArrayList<>();
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			Map<byte[], byte[]> result = jedis.hgetAll(key.getBytes("UTF-8"));
			Set<byte[]> keys = result.keySet();

			if (null == keys)
			{
				//log.cacheInfo("HGet Key：" + key + " missed!");
				return null;
			}
			//log.cacheInfo("HGet Key：" + key + " hit!");

			for (Iterator<byte[]> iter = keys.iterator(); iter.hasNext();)
			{
				byte[] field = iter.next();
				byte[] valueBytes = result.get(field);
				ret.add((E) SerializeUtil.hessian2Deserialize(valueBytes));
			}
			return ret;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianHgetAll", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 获取hash中的单个对象
	 * @param key
	 * @return
	 */
	@Override
	public Object hessianHget(String key, String field)
	{
		if (StringUtils.isBlank(key) || StringUtils.isBlank(field))
		{
			return null;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] valueBytes = jedis.hget(key.getBytes("UTF-8"), field.getBytes("UTF-8"));
			if (null == valueBytes)
			{
				return null;
			}
			return SerializeUtil.hessian2Deserialize(valueBytes);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianHget", "exception", "key", key, "field", field);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 设置值的时候加锁，如果值被改变，则设置失败
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public boolean hessianSetWithLock(String key, Object value)
	{
		if (null == key || null == value)
		{
			return false;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			long start = System.currentTimeMillis();
			jedis.watch(key);
			Transaction tx = jedis.multi();
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.hessian2Serialize(value);
			tx.set(keyBytes, valueBytes).toString();
			if (tx.exec() == null)
			{
				LogUtil.INFO(log, "RedisUtil", "hessianSetWithLock", "modifiedByOtherThread", "info", "key", key);
				return false;
			}
			jedis.unwatch();
			long end = System.currentTimeMillis();
			return true;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianSetWithLock", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 发布订阅，序列化发布
	 * @param channel
	 * @param message
	 * @return
	 */
	@Override
	public boolean hessianPublish(String channel, Object message)
	{
		if (null == channel || null == message)
		{
			return false;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			byte[] channelByte = channel.getBytes("UTF-8");
			byte[] messageByte = SerializeUtil.hessian2Serialize(message);
			Long ret = jedis.publish(channelByte, messageByte);
			return true;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hessianPublish", "exception", "channel", channel);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 发布订阅
	 * @param channel
	 * @param message
	 * @return
	 */
	@Override
	public long jsonPublish(String channel, Object message)
	{
		if (null == channel || null == message)
		{
			return -1;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String messageStr = JacksonUtils.obj2json(message);

			Long result = jedis.publish(channel, messageStr);
			return result;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "jsonPublish", "exception", "channel", channel);
			redisPool.returnBrokenResource(jedis);
			return -1;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	//	static ThreadLocal<String> lockThreadLocal = new ThreadLocal<String>();
	/**
	 * 分布式锁的key前缀
	 */
	public static final String KEY_LOCK = "lock:";

	/**
	 * 获取锁，基于redis的setex<br>
	 * 你有最长4秒完成锁定后的操作，逾期锁字段将被删除，其他线程\进程可以进入<br>
	 * @param id 锁定的id
	 * @param returnImmediately 是立刻返回结果还是等待直到获取锁，后者总会拿到true，但不保证时间
	 * @return true 获得锁 false 无法获得锁
	 */
	@Override
	public boolean getLock(String id, boolean returnImmediately)
	{
		int expireSecs = 4;
		String key = KEY_LOCK + id;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String result = jedis.set(key, String.valueOf(System.currentTimeMillis()), "NX", "EX", expireSecs);
			LogUtil.INFO(log, "RedisUtil", "getLock", "set", "info", "key", key, "result", result);
			if (null != result && "OK".equalsIgnoreCase(result))
			{
				return true;
			}
			else if (returnImmediately)
			{
				//当存入的值与当前时间相差180s以上的时候 本应该超时的key没有超时 这里强制设置超时时间为0 并且返回true
				String expireStr = jedis.get(key);
				Long now = System.currentTimeMillis();
				String nowStr = String.valueOf(now);
				if (!StringUtils.isNumeric(expireStr))
				{
					return false;
				}
				Long expireLong = Long.parseLong(expireStr);
				if (now - expireLong > expireSecs * 1000)
				{
					Long d = jedis.del(key);
					String s = jedis.set(key, String.valueOf(System.currentTimeMillis()), "NX", "EX", expireSecs);
					return true;
				}
				return false;
			}

			do
			{
				TimeUnit.MILLISECONDS.sleep(50);
				String value = jedis.get(key);
				if (StringUtils.isEmpty(value))
				{
					result = jedis.set(key, String.valueOf(System.currentTimeMillis()), "NX", "EX", expireSecs);
					LogUtil.INFO(log, "RedisUtil", "getLock", "setagain", "info", "key", key, "result", result);
					if (null != result && "OK".equalsIgnoreCase(result))
					{
						return true;
					}
				}
				long lockTime = Long.parseLong(value);
				if (lockTime + expireSecs * 1000 < System.currentTimeMillis())
				{
					return false;
				}
			}
			while (!"OK".equalsIgnoreCase(jedis.set(key, String.valueOf(System.currentTimeMillis()), "NX", "EX",
					expireSecs)));
			Long e = jedis.expire(key, expireSecs);
			return true;
		}
		catch (NumberFormatException e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "getLock", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		catch (InterruptedException e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "getLock", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		catch (Throwable e)
		{
			LogUtil.ERROR(log, null, "RedisUtil", "getLock", "Throwable", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void subscribe(JedisPubSub redisPubListener, String... channels)
	{

		Jedis jedis = redisPool.getRedisClient();
		try
		{
			jedis.subscribe(redisPubListener, channels);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "subscribe", "exception", "channels", channels);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	/**
	 * 释放锁
	 * @param id
	 */
	@Override
	public void releaseLock(String id)
	{
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String key = KEY_LOCK + id;
			Long ret = jedis.del(key);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "releaseLock", "exception", "id", id);
			redisPool.returnBrokenResource(jedis);
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	@Override
	public Long setnx(String key, int seconds, String value)
	{
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			long ret = jedis.setnx(key, value);
			Long e = jedis.expire(key, seconds);
			return ret;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "setnx", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}

	}

	@Override
	public Long zrem(String key, String... members)
	{
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrem(key, members);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrem", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max)
	{
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.zrangeByScoreWithScores(key, min, max);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "zrangeByScoreWithScores", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public Long lpush(String key, String... strings)
	{
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.lpush(key, strings);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "lpush", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public boolean getLock(String id)
	{
		int expireSecs = 30;
		String key = KEY_LOCK + id;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			String result = jedis.set(key, String.valueOf(System.currentTimeMillis()), "NX", "EX", expireSecs);
			LogUtil.INFO(log, "RedisUtil", "getLock", "set", "info", "key", key, "result", result);
			if (null != result && "OK".equalsIgnoreCase(result))
			{
				//				jedis.expire(key, expireSecs);
				return true;
			}
			else
			{
				//ttl小于0 表示key上没有设置生存时间（key是不会不存在的，因为前面setnx会自动创建）            
				//如果出现这种状况，那就是进程的某个实例setnx成功后 crash 导致紧跟着的expire没有被调用			            
				//这时可以直接设置expire并把锁纳为己用
				//In Redis 2.6 or older, if the Key does not exists or does not have an associated expire, -1 is returned. 
				//In Redis 2.8 or newer, if the Key does not have an associated expire, -1 is returned or if the Key does not exists, -2 is returned.
				long ttlValue = jedis.ttl(key);
				if (ttlValue < 0)
				{
					Long s = jedis.setnx(key, String.valueOf(System.currentTimeMillis()));
					Long e = jedis.expire(key, expireSecs);
					return true;
				}

				String expireStr = jedis.get(key);
				Long now = System.currentTimeMillis();
				String nowStr = String.valueOf(now);
				if (!StringUtils.isNumeric(expireStr))
				{
					return false;
				}
				Long expireLong = Long.parseLong(expireStr);
				if (now - expireLong > expireSecs * 1000)
				{
					jedis.del(key);
					jedis.setnx(key, nowStr);
					jedis.expire(key, expireSecs);
					return true;
				}
				return false;
			}
		}
		catch (NumberFormatException e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "getLock", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		catch (Throwable e)
		{
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public List<String> time()
	{
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.time();
		}
		catch (Exception e)
		{
			redisPool.returnBrokenResource(jedis);
			return null;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	@Override
	public long milTime()
	{
		List<String> redisTimeList = time();
		if (null == redisTimeList || redisTimeList.isEmpty())
		{
			return System.currentTimeMillis();
		}
		long redisMilTime = Long.parseLong(redisTimeList.get(0)) * 1000 + Long.parseLong(redisTimeList.get(1)) / 1000;
		return redisMilTime;
	}

	@Override
	public long hincrBy(String key, String field, long value)
	{
		if (StringUtils.isBlank(key) || StringUtils.isBlank(field))
		{
			return 0;
		}
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			long newValue = jedis.hincrBy(key, field, value);
			return newValue;
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "hincrBy", "exception", "key", key, "field", field, "value", value);
			redisPool.returnBrokenResource(jedis);
			return 0;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public boolean sismember(String key, String member)
	{
		if (StringUtils.isBlank(key))
			return false;
		Jedis jedis = redisPool.getRedisClient();
		try
		{
			return jedis.sismember(key, member);
		}
		catch (Exception e)
		{
			LogUtil.ERROR(log, e, "RedisUtil", "sismember", "exception", "key", key);
			redisPool.returnBrokenResource(jedis);
			return false;
		}
		finally
		{
			if (null != jedis && jedis.isConnected())
			{
				redisPool.returnResource(jedis);
			}
		}
	}
}
