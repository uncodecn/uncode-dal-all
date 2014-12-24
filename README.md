fastser-dal
===========

本着不重复造轮子的原则，基于mybatis、spring jdbc、hibernate等ORM的通用数据该问层，支持基于datasource的读写分离、主备自动切换和故障转移，支持简单的负载均衡。

# 功能概述

* 基于mybatis、spring jdbc、hibernate等各大orm框架实现通用dal层功能，并可以与已有项目完全兼容。
* 实现dal层cache
* 实现基于多数据源(datasource)的读写分离、主备切换、故障转移、恢复检测和负载均衡
* 使用该组件必须遵循以下规则：默认字段名称与数据库表字段一致，每张表有名称为id的唯一标识字段


## maven

		<dependency>
			<groupId>org.fastser</groupId>
		    <artifactId>fastser-dal</artifactId>
		    <version>1.0.1</version>
		</dependency>
        
        <dependency>
			<groupId>org.fastser</groupId>
		    <artifactId>fastser-util</artifactId>
		    <version>1.0.1</version>
		</dependency>
        
        <dependency>
			<groupId>org.fastser</groupId>
		    <artifactId>fastser-dal-mybatis</artifactId>
		    <version>1.0.1</version>
		</dependency>
        或者
        <dependency>
			<groupId>org.fastser</groupId>
		    <artifactId>fastser-dal-spring-jdbc</artifactId>
		    <version>1.0.1</version>
		</dependency>
        
		
		

### 与spring集成

动态数据源可以自动处理故障转移和恢复检测，读写分离时多个读库采用随机获取。缓存可以自定义实现，可以统一开启或关闭，方便在开发环境使用。

		<!-- 缓存默认实现，可以自定义实现 -->
        <bean id="cacheManager" class="org.fastser.dal.cache.support.SimpleCacheManager"></bean>
        <!-- 配置缓存 -->
		<bean id="cacheManager" class="org.fastser.dal.cache.support.SimpleCacheManager">
			<property name="cache" ref="dalRedisCache"></property>
		</bean>
    	<!-- 配置数据源解析器 -->
    	<bean id="resolveDatabase" class="org.fastser.dal.descriptor.db.impl.SimpleResolveDatabase">
        	<property name="dataSource" ref="dataSource" />
        	<property name="cacheManager" ref="cacheManager" />
    	</bean>
    
    	<!-- spring jdbc实现配置 -->
		<bean id="commonJdbcSupport" class="org.fastser.dal.spring.jdbc.CommonJdbcSupport">
        	<property name="dataSource" ref="dataSource" />
    	</bean>    
    	<bean id="baseDAL" class="org.fastser.dal.spring.jdbc.SpringJDBCDAL">
        	<property name="cacheManager" ref="cacheManager" />
        	<property name="commonJdbcSupport" ref="commonJdbcSupport" />
        	<property name="resolveDatabase" ref="resolveDatabase" />
            <!-- 可选，配置全局不使用缓存，默认为true -->
            <property name="useCache" value="false" />
            <!-- 可选，乐观锁，如果配置该项并且表中存在字段名为ver的字段，则该表可使用乐观锁机制 -->
			<property name="version" value="ver" />
    	</bean>
        
    	<!-- mybatis实现配置 -->
    	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    		<property name="dataSource" ref="dataSource" />  
		</bean>
    	<bean id="commonMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">  
	    	<property name="sqlSessionFactory" ref="sqlSessionFactory" />  
	    	<property name="mapperInterface" value="org.fastser.dal.mybatis.CommonMapper" />  
		</bean>
		<bean id="baseDAL" class="org.fastser.dal.mybatis.MybatisDAL">
            <property name="cacheManager" ref="cacheManager" />
        	<property name="commonMapper" ref="commonMapper" />
        	<property name="resolveDatabase" ref="resolveDatabase" />
            <!-- 可选，配置全局不使用缓存，默认为true -->
            <property name="useCache" value="false" />
            <!-- 可选，乐观锁，如果配置该项并且表中存在字段名为ver的字段，则该表可使用乐观锁机制 -->
			<property name="version" value="ver" />
    	</bean>

		<!-- hibernate实现配置 -->
        待实现

		<!--可选，动态数据源配置 -->
		<bean id="dynamicDataSource" class="org.fastser.dal.datasource.DynamicDataSource">
        	<!-- 从数据库配置，用于读操作，目前为随机取一个 -->
			<property name="slaveDataSources">
				<map key-type="java.lang.String">
					<entry key="readDataSourceOne" value-ref="dataSource3"/>
					<entry key="readDataSourceTwo" value-ref="dataSource4"/>
				</map>
			</property>
            <!-- 主数据库配置 -->
			<property name="masterDataSource" ref="dataSource1" />
            <!-- 备数据库配置 -->
			<property name="standbyDataSource" ref="dataSource2" />
		</bean>
## 使用示例

### 1 现有方法

* 列表查询

	fields需要显示的字段，queryCriteria查询条件，seconds缓存时间
	
    	QueryResult selectByCriteria(List<String> fields, QueryCriteria queryCriteria);
    
    	QueryResult selectByCriteria(String[] fields, QueryCriteria queryCriteria);
    
    	QueryResult selectByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds);
    
    	QueryResult selectByCriteria(String[] fields, QueryCriteria queryCriteria, int seconds);
    
    	QueryResult selectByCriteria(QueryCriteria queryCriteria);
    
        QueryResult selectByCriteria(QueryCriteria queryCriteria, int seconds);
    
    	QueryResult selectPageByCriteria(List<String> fields, QueryCriteria queryCriteria);
    
    	QueryResult selectPageByCriteria(String[] fields, QueryCriteria queryCriteria);
    
    	QueryResult selectPageByCriteria(List<String> fields, QueryCriteria queryCriteria, int seconds);
    
    	QueryResult selectPageByCriteria(String[] fields, QueryCriteria queryCriteria, int seconds);
    
    	QueryResult selectPageByCriteria(QueryCriteria queryCriteria);
    
    	QueryResult selectPageByCriteria(QueryCriteria queryCriteria, int seconds);
    
* 统计查询

	queryCriteria查询条件，seconds缓存时间
    
    	int countByCriteria(QueryCriteria queryCriteria);
    
    	int countByCriteria(QueryCriteria queryCriteria, int seconds);
    
* 主键查询

	Object带主键对象实体，fields需要显示的字段，queryCriteria查询条件，seconds缓存时间，clazz对象实例类型，id对象主键

    	QueryResult selectByPrimaryKey(Object obj);
    
		QueryResult selectByPrimaryKey(Object obj, int seconds);
    
    	QueryResult selectByPrimaryKey(List<String> fields, Object obj);
    
    	QueryResult selectByPrimaryKey(String[] fields, Object obj);
    
    	QueryResult selectByPrimaryKey(List<String> fields, Object obj, int seconds);
    
    	QueryResult selectByPrimaryKey(String[] fields, Object obj, int seconds);
    
    	QueryResult selectByPrimaryKey(Class<?> clazz, Object id);
    
    	QueryResult selectByPrimaryKey(Class<?> clazz, Object id, int seconds);
    
    	QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id);
    
    	QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id, int seconds);
        
* 插入

	Object带主键对象实体
    
    	int insert(Object obj);
* 更新

	Object带主键对象实体，queryCriteria查询条件

    	int updateByCriteria(Object obj, QueryCriteria queryCriteria);
    
    	int updateByPrimaryKey(Object obj);
        
* 删除

	Object带主键对象实体，queryCriteria查询条件，clazz对象实例类型，id对象主键

    	int deleteByPrimaryKey(Object obj);
    
    	int deleteByPrimaryKey(Class<?> clazz, Object id);
    
    	int deleteByCriteria(QueryCriteria queryCriteria);
    
* 其他操作

	database数据名称，tableName表名称
    
    	void reloadTable(String tableName);
    
    	void clearCache(String tableName);
    
    	void reloadTable(String database, String tableName);
    
    	void clearCache(String database, String tableName);

### 2 使用示例

* 数据库user表字段

		CREATE TABLE `user` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `name` varchar(20) DEFAULT NULL COMMENT '用户名',
      `pwd` varchar(50) DEFAULT NULL '密码'
      `email` varchar(30) DEFAULT NULL '邮箱',
      `status` int(11) DEFAULT '0' COMMENT '1正常0禁用',
      `age` int(11) DEFAULT NULL '年龄',
      PRIMARY KEY (`id`),
      UNIQUE KEY `unique_user_userName` (`userName`)
      ) ENGINE=MyISAM AUTO_INCREMENT=167 DEFAULT CHARSET=utf8;

* dto对象User.class

	    import java.io.Serializable;

    	public class User implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 4799201163494761002L;

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String PWD = "pwd";
        public static final String EMAIL = "email";
        public static final String STATUS = "status";
        public static final String AGE = "age";

        private int id;

        private String name;

        private String pwd;

        private String email;

        private int status;

        private int age;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
   	 }

* 列表查询
	* 示例1

			QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnGreaterThan(User.AGE, 18);
            critera.andColumnEqualTo(User.STATUS, 1);
            //查询所有字段并缓存
        	QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
            //查询部分字段不缓存
            QueryResult result =  baseDAL.selectByCriteria(new String[]{"name","pwd"},queryCriteria, BaseDal.NO_CACHE);
            List<User> users = result.asList(User.class);

	* 示例2

			QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	queryCriteria.setPageIndex(1);
        	queryCriteria.setPageSize(20);
        	queryCriteria.setOrderByClause(User.AGE+" desc");
            //分页查询
        	QueryResult result =  baseDAL.selectPageByCriteria(queryCriteria);
        	List<Map<String, Object>> list = result.getList();
        	Map<String, Object>  page = result.getPage();


* 主键查询
	* 示例1

			User user = new User();
        	user.setId(1);
            //缓存20秒
        	QueryResult result =  baseDAL.selectByPrimaryKey(user, 20);
            user = result.as(User.class);
	* 示例2

        	Model model = new Model(User.class);
        	model.setSinglePrimaryKey(1);
            //显示部分字段
        	QueryResult result =  baseDAL.selectByPrimaryKey(new String[]{"user","pwd"}, model);
            Map<String, Object> result = result.get();

    * 示例3
			//不使用缓存
	        QueryResult result =  baseDAL.selectByPrimaryKey(User.class, 1，BaseDal.NO_CAHCE);

* 插入
	* 示例1

			User user = new User();
        	user.setName("fastser");
            user.setPwd("faster");
        	int result = baseDAL.insert(user);

* 更新
	* 示例1

			User user = new User();
        	user.setName("fastser-dal-mybatis");
        	QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnEqualTo(User.NAME, "fastser");
        	int result = baseDAL.updateByCriteria(user, queryCriteria);

	* 示例2

        	User user = new User();
        	user.setEmail("ywj_316@qq.com");
        	user.setId(1);
        	int result = baseDAL.updateByPrimaryKey(user);

* 删除
	* 示例1

        	Model model = new Model(User.class);
        	model.setSinglePrimaryKey(1);
        	int result = baseDAL.deleteByPrimaryKey(model);

	* 示例2

        	User user = new User();
        	user.setId(1);
        	int result = baseDAL.deleteByPrimaryKey(user);

	* 示例3

        	int result = baseDAL.deleteByPrimaryKey(User.class, 165);

	* 示例4

        	QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnEqualTo(User.NAME, "fastser");
        	int result = baseDAL.deleteByCriteria(queryCriteria);

* 其他操作
	* 示例1

			QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnGreaterThan(User.AGE, 18);
            critera.andColumnEqualTo(User.STATUS, 1);
            //数量统计
        	int result =  baseDAL.countByCriteria(queryCriteria);


## 版权

作者：juny（ywj_316@qq.com）

技术支持QQ群：47306892

Copyright 2013 www.uncode.cn


