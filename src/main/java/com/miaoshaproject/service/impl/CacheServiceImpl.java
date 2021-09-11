package com.miaoshaproject.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaoshaproject.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {
    private Cache<String, Object> commonCache = null;

    @PostConstruct
    public void init(){
        //配置
        commonCache = CacheBuilder.newBuilder()
                //设置容量的初始容量为10
                .initialCapacity(10)
                .maximumSize(100)//最大可存储100个key，超过100个key，按照lru的策略移除缓存项
                .expireAfterWrite(60, TimeUnit.SECONDS).build();//60秒失效
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);//和map一样，put进去
    }

    @Override
    public Object getCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
