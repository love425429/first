package com.psk.first.common.redis;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterCommand;
import redis.clients.jedis.Protocol;

import com.psk.first.utils.ConfigUtil;

public class ClusterRedisPool implements IRedisType
{

	private String serverName = null;
	private ExtendsJedisCluster redisPool = null;

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
		redisPool = new ExtendsJedisCluster(jedisClusterNodes, timeout, jedisConfig);
	}

	@Override
	public Jedis getRedisClient()
	{
		return null;
	}

	@Override
	public ExtendsJedisCluster getRedisClusterClient()
	{
		return redisPool;
	}

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
		try
		{
			this.redisPool.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public String getServerName()
	{
		return this.serverName;
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
