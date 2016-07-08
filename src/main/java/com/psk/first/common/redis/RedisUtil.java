package com.psk.first.common.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Tuple;

import com.psk.first.common.LogUtil;
import com.psk.first.common.redis.ClusterRedisPool.ExtendsJedisCluster;
import com.psk.first.utils.ConfigUtil;
import com.psk.first.utils.SerializeUtil;

public class RedisUtil implements IRedisIOp
{
	private final static Log log = LogFactory.getLog(RedisUtil.class);

	private IRedisType redisPool = null;
	private boolean isRedisCluster = false;

	public RedisUtil(String serverName)
	{
		RedisObjectFactory redisObjectFactory = RedisObjectFactory.getInstance();
		this.redisPool = redisObjectFactory.build(serverName);
		if (this.redisPool != null && this.redisPool instanceof ClusterRedisPool)
		{
			isRedisCluster = true;
		}

	}

	@Override
	public String javaSet(String key, Object value)
	{
		if (null == key || null == value)
		{
			return null;
		}
		Jedis jedis = this.redisPool.getRedisClient();
		ExtendsJedisCluster jedisCluster = null;
		if (isRedisCluster)
		{
			jedisCluster = this.redisPool.getRedisClusterClient();
		}
		try
		{
			if (!isRedisCluster)
			{
				String password = ConfigUtil.getStringValueFromConfig(this.redisPool.getServerName() + ".password", "");
				if (!StringUtils.isEmpty(password))
				{
					String authResult = jedis.auth(password);
				}
			}
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = SerializeUtil.javaSerialize(value);
			return isRedisCluster ? jedisCluster.set(keyBytes, valueBytes) : jedis.set(keyBytes, valueBytes);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object javaGet(String key)
	{
		if (null == key)
		{
			return null;
		}
		Jedis jedis = this.redisPool.getRedisClient();
		ExtendsJedisCluster jedisCluster = null;
		if (isRedisCluster)
		{
			jedisCluster = this.redisPool.getRedisClusterClient();
		}
		try
		{
			if (!isRedisCluster)
			{
				String password = ConfigUtil.getStringValueFromConfig(this.redisPool.getServerName() + ".password", "");
				if (!StringUtils.isEmpty(password))
				{
					String authResult = jedis.auth(password);
				}
			}
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = isRedisCluster ? jedisCluster.get(keyBytes) : jedis.get(keyBytes);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String hessianSetex(String key, int seconds, Object value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object hessianGet(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String set(String key, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setex(String key, int seconds, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long setnx(String key, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long setnx(String key, int seconds, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mset(String[] keysvalues)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mset(byte[][] keysvalues)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> mget(String[] keys)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long incr(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long incrBy(String key, long value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExpireTime(String key, int seconds)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setExpireAt(String key, long unixTime)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> keys(String pattern)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getKeySetByPattern(String pattern)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> patternHessianGet(String pattern)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isKeyExist(String key)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void hessianDel(String key)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object delKey(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long delKeys(String[] keys)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hessianSetWithLock(String key, Object value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> zsetMembers(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zadd(String key, double score, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void zrem(String key, String value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Long zrem(String key, String... members)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zcard(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zcount(String key, double min, double max)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrange(String key, long start, long end)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void zcut(String key, int start, int end)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Double zscore(String key, String member)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zremrangebyrank(String key, long min, long max)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zrank(String key, String member)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zrevrank(String key, String member)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double zincr(String key, String member)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double zincrBy(String key, double score, String member)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String hget(String key, String field)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long hset(String key, String field, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hexists(String key, String field)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, String> hgetAll(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> hkeys(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> hvals(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> hmget(String key, String[] fields)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void hmset(String key, Map<String, String> hash)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Long hdel(String key, String... fields)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void hessianHset(String key, String field, Object value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public <E> List<E> hessianHgetAll(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object hessianHget(String key, String field)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> smembers(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long scard(String key)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long sadd(String key, String[] value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void srem(String key, String[] value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Long sadd(String key, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void srem(String key, String value)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String srandMember(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> srandMember(String key, int count)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String spop(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long smove(String srcKey, String dstKey, String member)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long lpush(String key, String... strings)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object rpop(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lpop(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getRange(String key, int begin, int end)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long lpush(String key, String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hessianPublish(String channel, Object message)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long jsonPublish(String channel, Object message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void subscribe(JedisPubSub redisPubListener, String... channels)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getLock(String id, boolean returnImmediately)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getLock(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void releaseLock(String id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> time()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long milTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long hincrBy(String key, String field, long value)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean sismember(String key, String member)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
