package cn.uncode.dal.spring.jdbc;

import javax.sql.DataSource;

import cn.uncode.dal.cache.CacheManager;
import cn.uncode.dal.core.BaseDAL;
import cn.uncode.dal.descriptor.db.ResolveDataBase;
import cn.uncode.dal.descriptor.db.impl.SimpleResolveDatabase;

public class SpringJDBCBuilder{
	
	protected CacheManager cacheManager;
    
	protected DataSource dataSource;
	
	protected boolean useCache;

    
    public SpringJDBCBuilder(DataSource dataSource) throws Exception {
        this.dataSource = dataSource;
    }
    

    public SpringJDBCBuilder setCacheManager(CacheManager cacheManager) {
        if (cacheManager != null) {
        	this.cacheManager = cacheManager;
        }
        return this;
    }
    
    public SpringJDBCBuilder setUseCache(boolean useCache) {
    	this.useCache = useCache;
        return this;
    }
    
    public BaseDAL buildDAL() {
    	ResolveDataBase resolveDataBase = new SimpleResolveDatabase();
    	resolveDataBase.setDataSource(dataSource);
    	if(null != cacheManager){
    		resolveDataBase.setCacheManager(cacheManager);
    	}
    	CommonJdbcSupport commonJdbcSupport = new CommonJdbcSupport();
		commonJdbcSupport.setDataSource(dataSource);
		SpringJDBCDAL baseDAL = new SpringJDBCDAL();
		baseDAL.setUseCache(useCache);
		baseDAL.setCommonJdbcSupport(commonJdbcSupport);
		baseDAL.setResolveDatabase(resolveDataBase);
        return baseDAL;
    }

	

}
