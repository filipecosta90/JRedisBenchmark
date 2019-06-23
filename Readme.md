# JRedisBenchmark

Microbenchmarking distinct Java Redis Clients with JMH

The same client instance will be shared across all threads running the same test.
By doing so we will test multithreaded performance of the state objects ( different clients ).

Java Redis Clients included:
- [Jedis](https://github.com/xetorthio/jedis)

## Getting started 
```
git clone https://github.com/filipecosta90/JRedisBenchmark.git
cd JRedisBenchmark
mvn clean package
```

## Simple Test with 1M requests, pipeline of 10, 50 Threads, datasize of 1, with 10 random keys
Note: make sure that the number of ops per iteration is set to be equal to the pipeline size. 
```console
java -jar target/JRedisBenchmark.jar "JRedisBenchmark"  -i 1 -wi 0 -f 1 -t 50 -r 10 -opi 10 -p pipeline=10 -p randomkeys=10
```

```console
(...)
Result "com.fcosta_oliveira.JRedisBenchmark.JRedisBenchmark.Jedis_SET":
  97.940 us/op


# Run complete. Total time: 00:00:42

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                  (auth)  (datasize)  (dbnum)   (hostip)  (hostport)  (pipeline)  (randomkeys)  (requests)   Mode  Cnt   Score   Error   Units
JRedisBenchmark.JRedisBenchmark.Jedis_GET                   1        0  127.0.0.1        6379          10            10     1000000  thrpt        0.538          ops/us
JRedisBenchmark.JRedisBenchmark.Jedis_SET                   1        0  127.0.0.1        6379          10            10     1000000  thrpt        0.540          ops/us
JRedisBenchmark.JRedisBenchmark.Jedis_GET                   1        0  127.0.0.1        6379          10            10     1000000   avgt       91.771           us/op
JRedisBenchmark.JRedisBenchmark.Jedis_SET                   1        0  127.0.0.1        6379          10            10     1000000   avgt       97.940           us/op

```
