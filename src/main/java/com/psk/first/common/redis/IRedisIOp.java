package com.psk.first.common.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Tuple;

public interface IRedisIOp
{
	/**===============================Redis基本操作：字符串或序列化对象存储===============================*/
	public String javaSet(String key, Object value);

	public String javaSetex(String key, int seconds, Object value);

	public Object javaGet(String key);

	public String hessianSet(String key, Object value);

	public String hessianSetex(String key, int seconds, Object value);

	public Object hessianGet(String key);

	public String set(String key, String value);

	public String setex(String key, int seconds, String value);

	public Long setnx(String key, String value);

	public Long setnx(String key, int seconds, String value);

	public Object get(String key);

	/**
	 * mset相当于 jedis.set("name","minxr"); jedis.set("jarorwar","aaa");  
	        jedis.mset("name", "minxr", "jarorwar", "aaa");  
	        System.out.println(jedis.mget("name", "jarorwar"));  
	 * @param keysvalues
	 * @return
	 */
	public String mset(String[] keysvalues);

	public String mset(byte[][] keysvalues);

	public List<String> mget(String[] keys);

	public Long incr(String key);

	public Long incrBy(String key, long value);

	public void setExpireTime(String key, int seconds);

	public void setExpireAt(String key, long unixTime);

	public Set<String> keys(String pattern);

	public Set<String> getKeySetByPattern(String pattern);

	/**
	 * 模糊查询：需要保证pattern查询得到的value类型一致，并在外部对模糊查询结果进行合并
	 */
	public List<Object> patternHessianGet(String pattern);

	public boolean isKeyExist(String key);

	public void hessianDel(String key);

	public Object delKey(String key);

	public Long delKeys(String[] keys);

	/**
	 * 设置值的时候加锁，如果值被改变，则设置失败
	 */
	public boolean hessianSetWithLock(String key, Object value);

	/**==============================Redis有序集合操作============================*/
	public Set<String> zsetMembers(String key);

	public Long zadd(String key, double score, String value);

	public void zrem(String key, String value);

	public Long zrem(String key, String... members);

	public Long zcard(String key);

	public Long zcount(String key, double min, double max);

	public Set<String> zrange(String key, long start, long end);

	public Set<Tuple> zrangeWithScores(String key, long start, long end);

	public Set<String> zrangeByScore(String key, double min, double max);

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

	public Set<String> zrevrange(String key, long start, long end);

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end);

	/**
	 * 删除有序集合中start到end之间的元素
	 */
	public void zcut(String key, int start, int end);

	/**
	 * 获取有序集合中member对应的score
	 */
	public Double zscore(String key, String member);

	/**
	 * 删除一定排名范围内的元素
	 */
	public Long zremrangebyrank(String key, long min, long max);

	public Long zrank(String key, String member);

	public Long zrevrank(String key, String member);

	/**
	 * 对sorted set的score加1
	 */
	public Double zincr(String key, String member);

	public Double zincrBy(String key, double score, String member);

	/**======================Redis哈希压缩存储================================*/
	public String hget(String key, String field);

	public Long hset(String key, String field, String value);

	public boolean hexists(String key, String field);

	public Map<String, String> hgetAll(String key);

	public Set<String> hkeys(String key);

	public List<String> hvals(String key);

	public List<String> hmget(String key, String[] fields);

	public void hmset(String key, Map<String, String> hash);

	public Long hdel(String key, String... fields);

	public void hessianHset(String key, String field, Object value);

	public <E> List<E> hessianHgetAll(String key);

	public Object hessianHget(String key, String field);

	/**==========================start Redis集合操作==================*/
	public Set<String> smembers(String key);

	public long scard(String key);

	public Long sadd(String key, String[] value);

	public void srem(String key, String[] value);

	public Long sadd(String key, String value);

	public void srem(String key, String value);

	/**
	 * 返回集合中的一个随机元素。
	 */
	public String srandMember(String key);

	/**
	 * 返回集合中的多个随机元素。如果 count 大于等于集合基数，那么返回整个集合。
	 */
	public List<String> srandMember(String key, int count);

	/**
	 * 移除并返回set集合中的一个随机元素。当 key 不存在或 key 是空集时，返回 nil 。
	 */
	public String spop(String key);

	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。原子操作
	 * 如果 source 集合不存在或不包含指定的 member 元素，则 SMOVE 命令不执行任何操作，仅返回 0 
	 */
	public Long smove(String srcKey, String dstKey, String member);

	/**==========================endof Redis 集合操作==================*/

	/**===================Redis list相关操作=============================*/
	public Long lpush(String key, String... strings);

	public Object rpop(String key);

	public Object lpop(String key);

	public List<String> getRange(String key, int begin, int end);

	public Long lpush(String key, String value);

	/**
	 * =======================Redis 发布订阅相关=========================================
	 */
	/**
	 * 发布订阅，序列化发布
	 */
	public boolean hessianPublish(String channel, Object message);

	/**
	 * 通过json方式发布
	 * 发布订阅
	 */
	public long jsonPublish(String channel, Object message);

	public void subscribe(JedisPubSub redisPubListener, String... channels);

	/**
	 * =======================分布式锁实现相关的key前缀=============================
	 */
	public static final String KEY_LOCK = "lock:";

	/**
	 * 获取锁，基于redis的setex<br>
	 * 你有最长1分钟完成锁定后的操作，逾期锁字段将被删除，其他线程\进程可以进入<br>
	 * @param id 锁定的id
	 * @param returnImmediately 是立刻返回结果还是等待直到获取锁，后者总会拿到true，但不保证时间
	 * @return true 获得锁 false 无法获得锁
	 */
	public boolean getLock(String id, boolean returnImmediately);

	/**
	 * 获取锁，基于redis的setnx<br> 需要逻辑上释放锁
	 * @param id 锁定的id
	 * @return true 获得锁 false 无法获得锁
	 */
	public boolean getLock(String id);

	/**
	 * 释放锁
	 * @param id
	 */
	public void releaseLock(String id);

	public List<String> time();

	/**
	 * 获取毫秒数
	 * @return
	 */
	public long milTime();

	long hincrBy(String key, String field, long value);

	boolean sismember(String key, String member);
}
