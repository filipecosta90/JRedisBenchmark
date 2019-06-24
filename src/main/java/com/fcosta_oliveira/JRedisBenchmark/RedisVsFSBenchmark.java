package com.fcosta_oliveira.JRedisBenchmark;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
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

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

//jedis


// An instance will be shared across all threads running the same test.
// By doing so we will also test multithreaded performance of the state objects ( different clients ).
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class RedisVsFSBenchmark {

    static final int records = 100000;
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    static Options opt;
    final Kryo kryo = new Kryo();
    final Output output = new Output(1024 * 512);
    final Input input = new Input(output.getBuffer());
    // pipeline size
    @Param({"1"})
    private int pipeline;
    @Param({"127.0.0.1"})
    private String hostip;
    @Param({"6379"})
    private int hostport;
    @Param({""})
    private String auth;
    @Param({"0"})
    private int dbnum;
    // datasize
    @Param({"1"})
    private int datasize;
    private String data;
    private JedisPool pool;

    public static void main(String[] args) throws RunnerException {

        opt = new OptionsBuilder()
                .include(RedisVsFSBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();

    }

    @Setup
    public void setup(BenchmarkParams params) {


        data = StringUtils.repeat("x", datasize);

        kryo.register(String.class);

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
    @OperationsPerInvocation(records)
    public void Jedis_SET_kryo(Blackhole bh) {
        Jedis jedis = pool.getResource();
        if (pipeline == 1) {
            for (int recordnum = 0; recordnum < records; recordnum++) {
                output.setPosition(0);
                kryo.writeObject(output, data);
                output.close();
                jedis.set(BigInteger.valueOf(recordnum).toByteArray(), output.toBytes());
            }
        } else {
            for (int recordnum = 0; recordnum < records; recordnum++) {
                Pipeline p = jedis.pipelined();
                for (int opNum = 0; opNum < pipeline; opNum++) {
                    output.setPosition(0);
                    kryo.writeObject(output, data);
                    output.close();
                    p.set(BigInteger.valueOf(recordnum).toByteArray(), output.toBytes());
                }
                p.sync();
            }
        }
        jedis.close();
    }

    @Benchmark
    @OperationsPerInvocation(records)
    public void Jedis_SET_plaint(Blackhole bh) {
        Jedis jedis = pool.getResource();
        if (pipeline == 1) {
            for (int recordnum = 0; recordnum < records; recordnum++) {
                jedis.set(String.valueOf(recordnum), data);
            }
        } else {
            for (int recordnum = 0; recordnum < records; recordnum++) {
                Pipeline p = jedis.pipelined();
                for (int opNum = 0; opNum < pipeline; opNum++) {
                    p.set(String.valueOf(recordnum), data);
                }
                p.sync();
            }
        }
        jedis.close();
    }

}