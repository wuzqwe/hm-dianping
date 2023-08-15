package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryAllShopType() {
        String key = RedisConstants.CACHE_SHOP_TYPE_KEY;
        //1.查询redis获取商品类型信息
        String strJosn = stringRedisTemplate.opsForValue().get(key);
        //2.在redis缓存中能查到
        if (StrUtil.isNotBlank(strJosn)){
            //3.存在则返回
            List<ShopType> shopTypes = JSONUtil.toList(JSONUtil.parseArray(strJosn), ShopType.class);
            return Result.ok(shopTypes);
        }
        //4.若不存在则从数据库中查找
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        if (shopTypes==null){
            return Result.fail("不存在");
        }
        //5.存在，将数据写入redis缓存中
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shopTypes));

        //6.返回
        return Result.ok(shopTypes);
    }
}
