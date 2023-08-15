package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

//生成全局id
@Component
public class RedisIdWorker {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 序列号的位数
     */
    private static  final int COUNT_BITS=32;

    private static final long BEGIN_TIMESTAMP=1685404800L;
    public long nextId(String keyPrefix){
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSeconds = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp=nowSeconds- BEGIN_TIMESTAMP;
        //2.生成序列号
        //2.1获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //2.2 自增长
        long count=stringRedisTemplate.opsForValue().increment("icr:"+keyPrefix+":"+date);

        //3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }

    public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(2023, 5, 30, 0, 0, 0);
        long l = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println(l);

        LocalDateTime now = LocalDateTime.now();
        String yyyyMMdd = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        System.out.println(yyyyMMdd);

        System.out.println();
    }
}
