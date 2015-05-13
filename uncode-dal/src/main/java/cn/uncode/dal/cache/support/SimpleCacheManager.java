package cn.uncode.dal.cache.support;

import cn.uncode.dal.cache.Cache;
import cn.uncode.dal.cache.CacheManager;
import cn.uncode.dal.cache.impl.ConcurrentMapCache;
import org.springframework.beans.factory.InitializingBean;

public class SimpleCacheManager implements InitializingBean, CacheManager {
    
    private Cache cache;

    /**
     * Specify the Cache instances to use for this CacheManager.
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

 

    public Cache getCache() {
        return cache;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        if(cache == null){
            cache = new ConcurrentMapCache();
        }
    }

}
