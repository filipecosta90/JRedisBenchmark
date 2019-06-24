# JRedisBenchmark

Microbenchmarking distinct Java Redis Clients with JMH

The same client instance will be shared across all threads running the same test.
By doing so we will test multithreaded performance of the state objects ( different clients ).

Java Redis Clients included:
- [Jedis](https://github.com/xetorthio/jedis)


Java Serializers included:
- [kryo](https://github.com/EsotericSoftware/kryo)

## Getting started 
```
git clone https://github.com/filipecosta90/JRedisBenchmark.git
cd JRedisBenchmark
mvn clean package
```

## Simple Test ( 5 repetitions ) with 1M requests, pipeline of 100, 1 Threads, datasize of 2048, blocksize of 1000
Note: make sure that the number of ops per iteration is set to be equal to the pipeline size. 
```console
mvn clean package && java -jar target/JRedisBenchmark.jar "RedisVsFSBenchmark"  -i 5 -wi 0 -f 1 -t 1  -p pipeline=100 -p datasize=2048 -p blocksize=1000
```

```console
(...)
# Run complete. Total time: 00:05:03

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                                      (auth)  (blocksize)  (datasize)  (dbnum)  (filesize)   (hostip)  (hostport)  (pipeline)   Mode  Cnt    Score    Error   Units
JRedisBenchmark.RedisVsFSBenchmark.FS_kryo_writeBlocks                        1000        2048        0   157440000  127.0.0.1        6379         100  thrpt    5   55.108 ± 28.158  ops/ms
JRedisBenchmark.RedisVsFSBenchmark.Jedis_SET_kryo_writeBlocks                 1000        2048        0   157440000  127.0.0.1        6379         100  thrpt    5  351.909 ± 75.387  ops/ms
JRedisBenchmark.RedisVsFSBenchmark.FS_kryo_writeBlocks                        1000        2048        0   157440000  127.0.0.1        6379         100   avgt    5    0.019 ±  0.011   ms/op
JRedisBenchmark.RedisVsFSBenchmark.Jedis_SET_kryo_writeBlocks                 1000        2048        0   157440000  127.0.0.1        6379         100   avgt    5    0.003 ±  0.001   ms/op
```
