package com.fcosta_oliveira.JRedisBenchmark;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

//jedis


// An instance will be shared across all threads running the same test.
// By doing so we will also test multithreaded performance of the state objects ( different clients ).
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class JRedisBenchmark {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    // pipeline size
    @Param({"1"})
    private int pipeline;
    static Options opt;
    @Param({"127.0.0.1"})
    private String hostip;
    @Param({"6379"})
    private int hostport;
    @Param({""})
    private String auth;
    @Param({"1000000"})
    private int requests;
    @Param({"0"})
    private int dbnum;
    // number of randomkeys
    @Param({"1"})
    private int randomkeys;
    // datasize
    @Param({"1"})
    private int datasize;
    private JedisPool pool;
    private String data;
    private ArrayList<String> keys;

    public static void main(String[] args) throws RunnerException {

        opt = new OptionsBuilder()
                .include(JRedisBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();

    }

    @Setup
    public void setup(BenchmarkParams params) {
        data = StringUtils.repeat("x", datasize);
        keys = new ArrayList<String>();
        for (int i = 0; i < randomkeys; i++) {
            keys.add("key:" + i);
        }
        if (auth.equals("")) {
            auth = null;
        }
        pool = new JedisPool(new JedisPoolConfig(), hostip, hostport, 2000, auth, dbnum);
        Jedis jedis = pool.getResource();
        LOG.info("Using pipeline size of " + pipeline);
        LOG.info("Started Flush DB");
        jedis.flushDB();
        LOG.info("Finished Flush DB");
        jedis.close();
    }

    @Benchmark
    public void Jedis_SET(Blackhole bh) {
        Jedis jedis = pool.getResource();
        if (pipeline == 1) {
            jedis.set(keys.get(ThreadLocalRandom.current().nextInt(0, randomkeys)), data);
        } else {
            Pipeline p = jedis.pipelined();
            for (int opNum = 0; opNum < pipeline; opNum++) {
                p.set(keys.get(ThreadLocalRandom.current().nextInt(0, randomkeys)), data);
            }
            p.sync();
        }
        jedis.close();
    }

    @Benchmark
    public void Jedis_GET(Blackhole bh) {
        Jedis jedis = pool.getResource();
        if (pipeline == 1) {
            jedis.get(keys.get(ThreadLocalRandom.current().nextInt(0, randomkeys)));
        } else {
            Pipeline p = jedis.pipelined();
            for (int opNum = 0; opNum < pipeline; opNum++) {
                p.get(keys.get(ThreadLocalRandom.current().nextInt(0, randomkeys)));
            }
            p.sync();
        }
        jedis.close();
    }

}