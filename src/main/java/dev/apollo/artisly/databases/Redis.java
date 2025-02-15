package dev.apollo.artisly.databases;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis {

    private String host;
    private int port;
    private String username;
    private String password;

    private JedisPool jedispool;
    private JedisPoolConfig jedisPoolConfig;

    public Redis(String host, int port, String username, String password) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(2);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        jedisPoolConfig.setBlockWhenExhausted(false);
        this.jedisPoolConfig = jedisPoolConfig;

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        if (username == null || username.isEmpty()) {
            this.jedispool = new JedisPool(jedisPoolConfig, host, port, 5000);
            return;
        }
        this.jedispool = new JedisPool(jedisPoolConfig, host, port, 5000, username, password, 0);
    }


    public JedisPool getJedis() {
        return jedispool;
    }
}