package com.fcosta_oliveira.JRedisBenchmark;

import com.esotericsoftware.kryo.Kryo;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

//jedis


// An instance will be shared across all threads running the same test.
// By doing so we will also test multithreaded performance of the state objects ( different clients ).
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class RedisVsFSBenchmark {

    static final int records = 1000000;
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    static Options opt;
    final Kryo kryo = new Kryo();
    private int bufferSize = 1024 * 1024;
    // Output for writing to a byte array.
    private Output output;
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

    // datasize
    @Param({"157440000"})
    private int filesize;

    @Param({"1000"})
    private int blocksize;

    public static void main(String[] args) throws RunnerException {

        opt = new OptionsBuilder()
                .include(RedisVsFSBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();

    }

    @Setup
    public void setup(BenchmarkParams params) {
        bufferSize = blocksize * datasize * pipeline;
        LOG.info("Using Kryo buffer size of " + bufferSize);
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
    public void Jedis_SET_kryo_writeBlocks(Blackhole bh) {

        output = new Output(bufferSize);
        Jedis jedis = pool.getResource();
        if (pipeline == 1) {
            int recordnum = 0;
            while (recordnum < records) {
                output.setPosition(0);
                int blockpos = 0;
                while (blockpos < blocksize && recordnum < records) {
                    kryo.writeObject(output, data);
                    blockpos++;
                    recordnum++;
                }
                // Flushes any buffered bytes and closes the underlying OutputStream, if any.
                output.close();
                jedis.set(BigInteger.valueOf(recordnum).toByteArray(), output.toBytes());
            }

        } else {
            int recordnum = 0;
            while (recordnum < records) {
                Pipeline p = jedis.pipelined();
                int opNum = 0;
                while (opNum < pipeline && recordnum < records) {
                    output.setPosition(0);
                    int blockpos = 0;
                    while (blockpos < blocksize && recordnum < records) {
                        kryo.writeObject(output, data);
                        blockpos++;
                        recordnum++;
                    }
                    opNum++;
                    // Flushes any buffered bytes and closes the underlying OutputStream, if any.
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
    public void FS_kryo_writeBlocks(Blackhole bh) {
        int filenum = 1;
        int recordnum = 0;
        while (recordnum < records) {

            try {
                Output foutput = new Output(new FileOutputStream("kryo_file" + filenum + ".dat"), bufferSize);
                foutput.setPosition(0);

                while ((foutput.total() < filesize) && (recordnum < records)) {
                    kryo.writeObject(foutput, data);
                    recordnum++;
                }
                //Flushes any buffered bytes and closes the underlying OutputStream, if any.
                foutput.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            filenum++;
        }
    }


//    @Benchmark
//    @OperationsPerInvocation(records)
//    public void Jedis_SET_kryo(Blackhole bh) {
//        Jedis jedis = pool.getResource();
//        if (pipeline == 1) {
//            for (int recordnum = 0; recordnum < records; recordnum++) {
//                output.setPosition(0);
//                kryo.writeObject(output, data);
//                output.close();
//                jedis.set(BigInteger.valueOf(recordnum).toByteArray(), output.toBytes());
//            }
//        } else {
//            for (int recordnum = 0; recordnum < records; recordnum++) {
//                Pipeline p = jedis.pipelined();
//                for (int opNum = 0; opNum < pipeline; opNum++) {
//                    output.setPosition(0);
//                    kryo.writeObject(output, data);
//                    output.close();
//                    p.set(BigInteger.valueOf(recordnum).toByteArray(), output.toBytes());
//                }
//                p.sync();
//            }
//        }
//        jedis.close();
//    }

//    /*@Benchmark
//    @OperationsPerInvocation(records)
//    public void Jedis_SET_plaint(Blackhole bh) {
//        Jedis jedis = pool.getResource();
//        if (pipeline == 1) {
//            for (int recordnum = 0; recordnum < records; recordnum++) {
//                jedis.set(String.valueOf(recordnum), data);
//            }
//        } else {
//            for (int recordnum = 0; recordnum < records; recordnum++) {
//                Pipeline p = jedis.pipelined();
//                for (int opNum = 0; opNum < pipeline; opNum++) {
//                    p.set(String.valueOf(recordnum), data);
//                }
//                p.sync();
//            }
//        }
//        jedis.close();
//    }*/

}