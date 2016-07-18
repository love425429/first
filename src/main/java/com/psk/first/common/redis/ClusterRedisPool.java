package com.psk.first.common.redis;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.el.MethodNotFoundException;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterCommand;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;

import com.psk.first.utils.ConfigUtil;

public class ClusterRedisPool implements IRedisType
{

	private String serverName = null;
	//	private ExtendsJedisCluster redisPool = null;
	private JedisCLusterAdapter redisPool = null;

	public ClusterRedisPool(String serverName)
	{
		this.serverName = serverName;
		this.init();
	}

	private void init()
	{
		/**JedisCluster stores pools for each node internally, and poolConfig is for setting up these pools.*/
		GenericObjectPoolConfig jedisConfig = new GenericObjectPoolConfig();
		// Number of connections to Redis that just sit there and do nothing
		jedisConfig.setMaxIdle(ConfigUtil.getIntValueFromConfig(getServerName() + ".maxIdle",
				GenericObjectPoolConfig.DEFAULT_MAX_IDLE));
		// Minimum number of idle connections to Redis - these can be seen as
		// always open and ready to serve
		jedisConfig.setMinIdle(ConfigUtil.getIntValueFromConfig(getServerName() + ".minIdle",
				GenericObjectPoolConfig.DEFAULT_MIN_IDLE));
		int timeout = ConfigUtil.getIntValueFromConfig(getServerName() + ".timeOut", Protocol.DEFAULT_TIMEOUT);
		jedisConfig.setMaxTotal(ConfigUtil.getIntValueFromConfig(getServerName() + ".maxTotal",
				GenericObjectPoolConfig.DEFAULT_MAX_TOTAL));
		jedisConfig.setMaxWaitMillis(timeout);
		jedisConfig.setTestOnBorrow(true);
		jedisConfig.setTestOnReturn(true);
		//			/**redis cluster服务地址*/
		String envClusterHosts = ConfigUtil.getStringValueFromConfig(getServerName() + ".cluster", "");
		Set<HostAndPort> jedisClusterNodes = parseHosts(envClusterHosts, new HashSet<HostAndPort>());
		redisPool = new JedisCLusterAdapter(new ExtendsJedisCluster(jedisClusterNodes, timeout, jedisConfig));
	}

	@Override
	public Jedis getRedisClient()
	{
		return redisPool;
	}

	/**
	 * 集群会自动回收
	 */
	@Override
	public void returnResource(Jedis j)
	{
	}

	@Override
	public void returnBrokenResource(Jedis j)
	{
	}

	@Override
	public void destroy()
	{
		this.redisPool.close();
	}

	@Override
	public String getServerName()
	{
		return this.serverName;
	}

	/**
	 * 适配器
	 * @author psk
	 *
	 */
	public class JedisCLusterAdapter extends Jedis
	{
		private ExtendsJedisCluster jedisPool = null;

		public JedisCLusterAdapter(ExtendsJedisCluster jedisPool)
		{
			this.jedisPool = jedisPool;
		}

		@Override
		public String set(byte[] key, byte[] value)
		{
			return this.jedisPool.set(key, value);
		}

		@Override
		public byte[] get(byte[] key)
		{
			return this.jedisPool.get(key);
		}

		@Override
		public Boolean exists(byte[] key)
		{
			return this.jedisPool.exists(key);
		}

		@Override
		public Long del(byte[] key)
		{
			return this.jedisPool.del(key);
		}

		@Override
		public Long del(String key)
		{
			return this.jedisPool.del(key);
		}

		@Override
		public Set<String> zrange(String key, long start, long end)
		{
			return this.jedisPool.zrange(key, start, end);
		}

		@Override
		public Long zadd(String key, double score, String value)
		{
			return this.jedisPool.zadd(key, score, value);
		}

		@Override
		public Long zrem(String key, String... value)
		{
			return this.jedisPool.zrem(key, value);
		}

		@Override
		public Long zcard(String key)
		{
			return this.jedisPool.zcard(key);
		}

		@Override
		public Long zcount(String key, double min, double max)
		{
			return this.jedisPool.zcount(key, min, max);
		}

		@Override
		public Set<Tuple> zrangeWithScores(String key, long start, long end)
		{
			return this.jedisPool.zrangeWithScores(key, start, end);
		}

		@Override
		public Set<Tuple> zrevrangeWithScores(String key, long start, long end)
		{
			return this.jedisPool.zrevrangeWithScores(key, start, end);
		}

		@Override
		public Long zremrangeByRank(String key, long start, long end)
		{
			return this.jedisPool.zremrangeByRank(key, start, end);
		}

		@Override
		public Double zscore(String key, String member)
		{
			return this.jedisPool.zscore(key, member);
		}

		@Override
		public Long zrank(String key, String member)
		{
			return this.jedisPool.zrank(key, member);
		}

		@Override
		public Long zrevrank(String key, String member)
		{
			return this.jedisPool.zrevrank(key, member);
		}

		@Override
		public String hget(String key, String field)
		{
			return this.jedisPool.hget(key, field);
		}

		@Override
		public Long hset(String key, String field, String value)
		{
			return this.jedisPool.hset(key, field, value);
		}

		@Override
		public Boolean hexists(String key, String field)
		{
			return this.jedisPool.hexists(key, field);
		}

		@Override
		public Map<String, String> hgetAll(String key)
		{
			return this.jedisPool.hgetAll(key);
		}

		@Override
		public Set<String> hkeys(String key)
		{
			return this.jedisPool.hkeys(key);
		}

		@Override
		public String hmset(String key, Map<String, String> hash)
		{
			return this.jedisPool.hmset(key, hash);
		}

		@Override
		public Set<String> zrangeByScore(String key, double min, double max)
		{
			return this.jedisPool.zrangeByScore(key, min, max);
		}

		@Override
		public List<String> hvals(String key)
		{
			return this.jedisPool.hvals(key);
		}

		@Override
		public List<String> hmget(String key, String... fields)
		{
			return this.jedisPool.hmget(key, fields);
		}

		@Override
		public Long hdel(String key, String... fields)
		{
			return this.jedisPool.hdel(key, fields);
		}

		@Override
		public Set<String> smembers(String key)
		{
			return this.jedisPool.smembers(key);
		}

		@Override
		public Long scard(String key)
		{
			return this.jedisPool.scard(key);
		}

		@Override
		public Long sadd(String key, String... value)
		{
			return this.jedisPool.sadd(key, value);
		}

		@Override
		public Set<String> zrevrange(String key, long start, long end)
		{
			return this.jedisPool.zrevrange(key, start, end);
		}

		@Override
		public Long del(String[] keys)
		{
			return this.jedisPool.del(keys);
		}

		@Override
		public Boolean sismember(String key, String member)
		{
			return this.jedisPool.sismember(key, member);
		}

		@Override
		public String setex(byte[] keyBytes, int seconds, byte[] valueBytes)
		{
			return this.jedisPool.setex(keyBytes, seconds, valueBytes);
		}

		@Override
		public Long srem(String key, String... value)
		{
			return this.jedisPool.srem(key, value);
		}

		@Override
		public String srandmember(String key)
		{
			return this.jedisPool.srandmember(key);
		}

		@Override
		public List<String> srandmember(String key, int count)
		{
			return this.jedisPool.srandmember(key, count);
		}

		@Override
		public String spop(String key)
		{
			return this.jedisPool.spop(key);
		}

		@Override
		public Long smove(String srcKey, String dstKey, String member)
		{
			return this.jedisPool.smove(srcKey, dstKey, member);
		}

		@Override
		public Long expire(String key, int seconds)
		{
			return this.jedisPool.expire(key, seconds);
		}

		@Override
		public Long expireAt(String key, long unixTime)
		{
			return this.jedisPool.expireAt(key, unixTime);
		}

		@Override
		public String set(String key, String value)
		{
			return this.jedisPool.set(key, value);
		}

		@Override
		public String setex(String key, int seconds, String value)
		{
			return this.jedisPool.setex(key, seconds, value);
		}

		@Override
		public Long setnx(String key, String value)
		{
			return this.jedisPool.setnx(key, value);
		}

		@Override
		public String get(String key)
		{
			return this.jedisPool.get(key);
		}

		@Override
		public String rpop(String key)
		{
			return this.jedisPool.rpop(key);
		}

		@Override
		public String lpop(String key)
		{
			return this.jedisPool.lpop(key);
		}

		@Override
		public List<String> lrange(String key, long begin, long end)
		{
			return this.jedisPool.lrange(key, begin, end);
		}

		@Override
		public Long lpush(String key, String... values)
		{
			return this.jedisPool.lpush(key, values);
		}

		@Override
		public String mset(String... keysvalues)
		{
			return this.jedisPool.mset(keysvalues);
		}

		@Override
		public Long ttl(String key)
		{
			return this.jedisPool.ttl(key);
		}

		@Override
		public List<String> time()
		{
			return this.jedisPool.time();
		}

		@Override
		public Long hincrBy(String key, String field, long value)
		{
			return this.jedisPool.hincrBy(key, field, value);
		}

		@Override
		public String mset(byte[]... keysvalues)
		{
			return this.jedisPool.mset(keysvalues);
		}

		@Override
		public List<String> mget(String... keys)
		{
			return this.jedisPool.mget(keys);
		}

		@Override
		public Long incr(String key)
		{
			return this.jedisPool.incr(key);
		}

		@Override
		public Long incrBy(String key, long value)
		{
			return this.jedisPool.incrBy(key, value);
		}

		@Override
		public Double zincrby(String key, double score, String member)
		{
			return this.jedisPool.zincrby(key, score, member);
		}

		@Override
		public Long hset(byte[] keyBytes, byte[] fieldBytes, byte[] valueBytes)
		{
			return this.jedisPool.hset(keyBytes, fieldBytes, valueBytes);
		}

		@Override
		public Map<byte[], byte[]> hgetAll(byte[] key)
		{
			return this.jedisPool.hgetAll(key);
		}

		@Override
		public byte[] hget(byte[] key, byte[] field)
		{
			return this.jedisPool.hget(key, field);
		}

		@Override
		public Long publish(byte[] channelByte, byte[] messageByte)
		{
			return this.jedisPool.publish(channelByte, messageByte);
		}

		@Override
		public Long publish(String channel, String message)
		{
			return this.jedisPool.publish(channel, message);
		}

		@Override
		public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max)
		{
			return this.jedisPool.zrangeByScoreWithScores(key, min, max);
		}

		@Override
		public void subscribe(JedisPubSub redisPubListener, String... channels)
		{
			this.jedisPool.subscribe(redisPubListener, channels);
		}

		@Override
		public String set(String key, String value, String nxxx, String expr, long time)
		{
			return this.jedisPool.set(key, value, nxxx, expr, time);
		}

		@Override
		public Set<String> keys(String pattern)
		{
			throw new MethodNotFoundException();
		}

		@Override
		public String watch(String... key)
		{
			throw new MethodNotFoundException();
		}

		@Override
		public String unwatch()
		{
			throw new MethodNotFoundException();
		}

		@Override
		public void close()
		{
			try
			{
				this.jedisPool.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 扩展JedisCluster以便支持一些特殊操作
	 * @author pengshukai
	 *
	 */
	public class ExtendsJedisCluster extends JedisCluster
	{
		public ExtendsJedisCluster(Set<HostAndPort> nodes)
		{
			super(nodes);
		}

		public ExtendsJedisCluster(Set<HostAndPort> nodes, int timeout)
		{
			super(nodes, timeout, DEFAULT_MAX_REDIRECTIONS);
		}

		public ExtendsJedisCluster(Set<HostAndPort> nodes, int timeout, int maxRedirections)
		{
			super(nodes, timeout, maxRedirections, new GenericObjectPoolConfig());
		}

		public ExtendsJedisCluster(Set<HostAndPort> nodes, final GenericObjectPoolConfig poolConfig)
		{
			super(nodes, DEFAULT_TIMEOUT, DEFAULT_MAX_REDIRECTIONS, poolConfig);
		}

		public ExtendsJedisCluster(Set<HostAndPort> nodes, int timeout, final GenericObjectPoolConfig poolConfig)
		{
			super(nodes, timeout, DEFAULT_MAX_REDIRECTIONS, poolConfig);
		}

		public ExtendsJedisCluster(Set<HostAndPort> jedisClusterNode, int timeout, int maxRedirections,
				final GenericObjectPoolConfig poolConfig)
		{
			super(jedisClusterNode, timeout, maxRedirections, poolConfig);
		}

		public ExtendsJedisCluster(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
				int maxRedirections, final GenericObjectPoolConfig poolConfig)
		{
			super(jedisClusterNode, connectionTimeout, soTimeout, maxRedirections, poolConfig);
		}

		/**增加JedisCluster对于time函数的支持*/
		public List<String> time()
		{
			return new JedisClusterCommand<List<String>>(connectionHandler, maxRedirections) {
				@Override
				public List<String> execute(Jedis connection)
				{
					return connection.time();
				}
			}.runWithAnyNode();
		}
	}

	private Set<HostAndPort> parseHosts(String envHosts, Set<HostAndPort> defaultHostsAndPorts)
	{

		if (null != envHosts && 0 < envHosts.length())
		{

			String[] hostDefs = envHosts.split(",");

			if (null != hostDefs && 2 <= hostDefs.length)
			{

				Set<HostAndPort> envHostsAndPorts = new HashSet<HostAndPort>(hostDefs.length);

				for (String hostDef : hostDefs)
				{

					String[] hostAndPort = hostDef.split(":");

					if (null != hostAndPort && 2 == hostAndPort.length)
					{
						String host = hostAndPort[0];
						int port = Protocol.DEFAULT_PORT;

						try
						{
							port = Integer.parseInt(hostAndPort[1]);
						}
						catch (final NumberFormatException nfe)
						{
						}

						envHostsAndPorts.add(new HostAndPort(host, port));
					}
				}

				return envHostsAndPorts;
			}
		}
		return defaultHostsAndPorts;
	}

}
