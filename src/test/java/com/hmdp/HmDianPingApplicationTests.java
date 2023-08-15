package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
class HmDianPingApplicationTests {


    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testSaveShop() throws InterruptedException {
        Shop shop = shopService.getById(1L);

        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY + 1L, shop, 10L, TimeUnit.SECONDS);
    }

    @Test
    void test2() {
        Long increment = stringRedisTemplate.opsForValue().increment("icd:" + "dll:" + "2020:1:3");
        System.out.println(increment);
    }

    @Test
    void testIdWork() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id =" + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();

        System.out.println("time=" + (end - begin));
    }

    @Test
    void loadShopData() {
        List<Shop> list = shopService.list();

        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));

        for (Map.Entry<Long, List<Shop>> entry :
                map.entrySet()) {

            Long typeId = entry.getKey();
            String key = "shop:geo:" + typeId;

            List<Shop> value = entry.getValue();

            List<RedisGeoCommands.GeoLocation<String>> location = new ArrayList<>();
            for (Shop shop : value) {
                location.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY()))
                );
            }
            stringRedisTemplate.opsForGeo().add(key, location);
        }
    }

    /**
     * UV统计
     * 独立客户量的访问量
     */
    @Test
    void testHyperLogLog() {
        String[] values=new String[1000];
        int j=0;
        for (int i = 0; i < 1000000; i++) {
            j=i%1000;
            values[j]="user_"+i;

            if (j==999){
                stringRedisTemplate.opsForHyperLogLog().add("hl2", values);
            }
        }
        //统计数量
        Long count = stringRedisTemplate.opsForHyperLogLog().size("hl2");
        System.out.println(count);
    }
}
