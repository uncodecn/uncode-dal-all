uncode-dal
===========

本着不重复造轮子的原则，基于mybatis、spring jdbc、hibernate等ORM的通用数据访问层，支持基于datasource的读写分离、主备自动切换和故障转移，支持简单的负载均衡。 


源码地址：http:http://git.oschina.net/uncode/uncode-dal-all

[TOC]



## 功能概述

* 基于mybatis、spring jdbc、hibernate等各大orm框架实现通用dal层功能，并可以与已有项目完全兼容。同时也可以在已经实现的orm框架之间任意切换，不需要对代码作任何改动。
* 实现dal层cache，可以自由设置过期时间。
* 实现基于多数据源(datasource)的读写分离、主备切换、故障转移、恢复检测和负载均衡
* 使用该组件必须遵循以下规则：
	* 在使用DTO时，字段名称与数据库表字段一致，也可以不使用任何DTO类。
	* 不支持连表查询，主要是单表操作，如果需要连表请与原生orm框架配合使用。
	* sql默认主键字段为id，nosql默认主键字段为_id。

## 使用场景

随着nosql等新方案的出现，关系型数据库更多的被用在业务数据存储中，对于一些像报表等复杂数据处理的工作大部分公司已经不再使用关系型数据库，而对于业务数据90%以上是不需要连表查询等复杂操作的，该组件设计目标是将这90%的工作量变成零，避免重复工作，提高开发速度。使用该组件后将不再需要mybatis的自动生成，spring jdbc的sql语句，hibernate的各种注解，因为组件可以忙你处理。同时该组件只是基于orm框架对所有单表操作的提取和封装，最终还是由相应的orm框架完成持久化工作，所以不会代替orm的工作。因此论你的项目是新项目，还是老项目都可以使用，不会对原有业务生产任何影响，只是使你的操作更加便利，不再需要无数dao类，只要在需要的service中注入即可。组件需要遵循一定的COC规则，所以存在一定的使用限制，请大家注意。

## maven

目前组件存放于oschina的maven库中，请在项目中添加以下地址：http://maven.oschina.net/content/repositories/thirdparty/

### 1 mybatis

		<dependency>
			<groupId>cn.uncode</groupId>
		    <artifactId>uncode-dal</artifactId>
		    <version>1.0.3</version>
		</dependency>
        <dependency>
			<groupId>cn.uncode</groupId>
		    <artifactId>uncode-dal-mybatis</artifactId>
		    <version>1.0.3</version>
		</dependency>
        
### 2 spring jdbc

		<dependency>
			<groupId>cn.uncode</groupId>
		    <artifactId>uncode-dal</artifactId>
		    <version>1.0.3</version>
		</dependency>
        <dependency>
			<groupId>cn.uncode</groupId>
		    <artifactId>uncode-dal-spring-jdbc</artifactId>
		    <version>1.0.3</version>
		</dependency>

### 3 mongo

		<dependency>
			<groupId>cn.uncode</groupId>
		    <artifactId>uncode-dal</artifactId>
		    <version>1.0.3</version>
		</dependency>
        <dependency>
			<groupId>cn.uncode</groupId>
		    <artifactId>uncode-dal-mongo</artifactId>
		    <version>1.0.3</version>
		</dependency>

jar文件下载地址：http://www.uncode.cn/uncode-dal/uncode-dal-all-1.0.3.zip

## spring集成

动态数据源可以自动处理故障转移和恢复检测，读写分离时多个读库采用随机获取。缓存可以自定义实现，可以统一开启或关闭，方便在开发环境使用。

### 1 DataSource配置

可以使用任意数据库连接池组件，建议使用Druid。配置Fastser-DAL动态数据源，将自动实现读写分离、主备自动切换和故障转移等，建议使用。使用Fastser-DAL动态数据源后，在spring jdbc/mybatis配置中将所有的ref="dataSource"换成ref="dynamicDataSource"即可。

		<!--可选，动态数据源配置 -->
		<bean id="dynamicDataSource" class="cn.uncode.dal.datasource.DynamicDataSource">
        	<!-- 从数据库配置，用于读操作，目前负载均衡机制为随机取一个 -->
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


### 2 Spring jdbc 实现配置

		<!-- 可选，缓存默认实现，集群时建议使用分布式缓存自定义实现 -->
        <bean id="dalCache" class="cn.uncode.dal.cache.impl.ConcurrentMapCache"></bean>
        <!-- 配置缓存 -->
		<bean id="cacheManager" class="cn.uncode.dal.cache.support.SimpleCacheManager">
			<property name="cache" ref="dalCache"></property>
		</bean>
    	<!-- 配置数据源解析器 -->
    	<bean id="resolveDatabase" class="cn.uncode.dal.descriptor.db.impl.SimpleResolveDatabase">
        	<property name="dataSource" ref="dataSource" />
        	<property name="cacheManager" ref="cacheManager" />
    	</bean>
    
    	<!-- spring jdbc实现配置 -->
		<bean id="commonJdbcSupport" class="cn.uncode.dal.spring.jdbc.CommonJdbcSupport">
        	<property name="dataSource" ref="dataSource" />
    	</bean>    
    	<bean id="baseDAL" class="cn.uncode.dal.spring.jdbc.SpringJDBCDAL">
        	<property name="cacheManager" ref="cacheManager" />
        	<property name="commonJdbcSupport" ref="commonJdbcSupport" />
        	<property name="resolveDatabase" ref="resolveDatabase" />
            <!-- 可选，配置全局不使用缓存，默认为true -->
            <property name="useCache" value="false" />
            <!-- 可选，乐观锁，如果配置该项并且表中存在字段名为ver的字段，则该表可使用乐观锁机制 -->
			<property name="version" value="ver" />
    	</bean>


### 3 Mybatis 实现配置

		<!-- 可选，缓存默认实现，集群时建议使用分布式缓存自定义实现 -->
        <bean id="dalCache" class="cn.uncode.dal.cache.impl.ConcurrentMapCache"></bean>
        <!-- 配置缓存 -->
		<bean id="cacheManager" class="cn.uncode.dal.cache.support.SimpleCacheManager">
			<property name="cache" ref="dalCache"></property>
		</bean>
    	<!-- 配置数据源解析器 -->
    	<bean id="resolveDatabase" class="cn.uncode.dal.descriptor.db.impl.SimpleResolveDatabase">
        	<property name="dataSource" ref="dataSource" />
        	<property name="cacheManager" ref="cacheManager" />
    	</bean>

    	<!-- mybatis实现配置 -->
    	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    		<property name="dataSource" ref="dataSource" />  
		</bean>
    	<bean id="commonMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">  
	    	<property name="sqlSessionFactory" ref="sqlSessionFactory" />  
	    	<property name="mapperInterface" value="cn.uncode.dal.mybatis.CommonMapper" />
		</bean>
		<bean id="baseDAL" class="cn.uncode.dal.mybatis.MybatisDAL">
            <property name="cacheManager" ref="cacheManager" />
        	<property name="commonMapper" ref="commonMapper" />
        	<property name="resolveDatabase" ref="resolveDatabase" />
            <!-- 可选，配置全局不使用缓存，默认为true -->
            <property name="useCache" value="false" />
            <!-- 可选，乐观锁，如果配置该项并且表中存在字段名为ver的字段，则该表可使用乐观锁机制 -->
			<property name="version" value="ver" />
    	</bean>

### 4 MongoDB 实现配置

		<!-- 可选，缓存默认实现，集群时建议使用分布式缓存自定义实现 -->
        <bean id="dalCache" class="cn.uncode.dal.cache.impl.ConcurrentMapCache"></bean>
        <!-- 可选，配置缓存 -->
		<bean id="cacheManager" class="cn.uncode.dal.cache.support.SimpleCacheManager">
			<property name="cache" ref="dalCache"></property>
		</bean>

    	<!-- mongo实现配置 -->
    	<bean id="mongoDataBase" class="cn.uncode.dal.mongo.MongoDataBase">
            <property name="host" value="172.16.30.98" />
            <property name="port" value="27017" />
            <property name="db" value="test" />
            <property name="username" value="xiaocong" />
			<property name="password" value="xiaocong" />
 		</bean>

        <bean id="mongoDAL" class="cn.uncode.dal.mongo.MongoDAL">
            <property name="database" ref="mongoDataBase"></property>
            <!-- 可选，配置全局不使用缓存，默认为true -->
            <property name="useCache" value="false" />
    	</bean>



## API

### 1 列表查询

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

### 2 统计查询

		queryCriteria查询条件，seconds缓存时间
    	int countByCriteria(QueryCriteria queryCriteria);
    	int countByCriteria(QueryCriteria queryCriteria, int seconds);
    
### 3 主键查询

Object带主键对象实体，fields需要显示的字段，queryCriteria查询条件，seconds缓存时间，clazz对象实例类型，id对象主键

        QueryResult selectByPrimaryKey(Object obj);
        QueryResult selectByPrimaryKey(Object obj, int seconds);
        QueryResult selectByPrimaryKey(List<String> fields, Object obj);
        QueryResult selectByPrimaryKey(String[] fields, Object obj);
        QueryResult selectByPrimaryKey(List<String> fields, Object obj, int seconds);
        QueryResult selectByPrimaryKey(String[] fields, Object obj, int seconds);
        QueryResult selectByPrimaryKey(String[] fields, String database, Object obj, int seconds);
        QueryResult selectByPrimaryKey(Class<?> clazz, Object id);
        QueryResult selectByPrimaryKey(String table, Object id);
        QueryResult selectByPrimaryKey(Class<?> clazz, Object id, int seconds);
        QueryResult selectByPrimaryKey(String table, Object id, int seconds);
        QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id);
        QueryResult selectByPrimaryKey(List<String> fields, String table, Object id);
        QueryResult selectByPrimaryKey(List<String> fields, Class<?> clazz, Object id, int seconds);
        QueryResult selectByPrimaryKey(List<String> fields, String table, Object id, int seconds);

### 4 插入

Object带主键对象实体

        Object insert(Object obj);
        Object insert(String table, Map<String, Object> obj);
        Object insert(String database, String table, Map<String, Object> obj);

### 5 更新

Object带主键对象实体，queryCriteria查询条件

        int updateByCriteria(Object obj, QueryCriteria queryCriteria);
        int updateByPrimaryKey(Object obj);
        int updateByPrimaryKey(String table, Map<String, Object> obj);
        int updateByPrimaryKey(String database, String table, Map<String, Object> obj);

### 6 删除

Object带主键对象实体，queryCriteria查询条件，clazz对象实例类型，id对象主键

        int deleteByPrimaryKey(Object obj);
        int deleteByPrimaryKey(String table, Map<String, Object> obj);
        int deleteByPrimaryKey(Class<?> clazz, Object id);
        int deleteByPrimaryKey(String table, Object id);
        int deleteByPrimaryKey(String database, String table, Object id);
        int deleteByCriteria(QueryCriteria queryCriteria);

### 7 其他操作

database数据名称，tableName表名称

    	void reloadTable(String tableName);
    	void clearCache(String tableName);
    	void reloadTable(String database, String tableName);
    	void clearCache(String database, String tableName);

### 8 QueryCriteria方法

		//排序，如：id desc, name
        setOrderByClause(String orderByClause)
        //去重
        setDistinct(boolean distinct)
        //只查询一条
        ssetSelectOne(boolean selectOne)
        //第几页，首页为1
        setPageSize(int pageSize)
        //设置表名
        setTable(String table)
        //设置数据库名称，一般不需要设置
        setDatabase(String database)
        //使用乐观锁时设置版本，只用于更新操作
        setVersion(Object version)
        //根据DTO对象类型设置名表，此时DTO类名必须与表名一致
        setTable(Class<?> clazz)

### 9 Criteria方法

		andColumnIsNull(String column)
        andColumnIsNotNull(String column)
        andColumnEqualTo(String column, Object value) 
        andColumnNotEqualTo(String column, Object value)
        andColumnGreaterThan(String column, Object value) 
        andColumnGreaterThanOrEqualTo(String column, Object value)
        andColumnLessThan(String column, Object value)
        andColumnLessThanOrEqualTo(String column, Object value)
        andColumnIn(String column, List<Object> values)
        andColumnNotIn(String column, List<Object> values)
        andColumnLike(String column, Object value) 
        andColumnNotLike(String column, Object value)
        andColumnBetween(String column, Object value1, Object value2)
        andColumnNotBetween(String column, Object value1, Object value2)


## 使用示例

### 1 User表结构


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

### 2 User.class对象

	    import java.io.Serializable;

    	public class User implements Serializable {

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

### 3 BaseDal注入

* 注解方式

		import cn.uncode.dal.core.BaseDAL;

        @Service
		public class UserService implements IUserService{

			@Autowired
    		private BaseDAL baseDAL;

        	.....
        ｝


* XML方式

		<bean id="userService" class="cn.uncode.web.service.UserService">
    		<property name="baseDAL" ref="baseDAL"></property>
    	</bean>

### 4 无DTO示例

* 列表查询
	* 示例1

			//创建查询对象
			QueryCriteria queryCriteria = new QueryCriteria();
            //没有DTO对象时可以直接设置表名
            //queryCriteria.setTable("user");
            //创建条件封装对象
        	Criteria critera = queryCriteria.createCriteria();
            //设置条件
        	critera.andColumnGreaterThan(“age”, 18);
            critera.andColumnEqualTo("status", 1);
            //查询所有字段并缓存
        	QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
            //查询部分字段缓存60称
            QueryResult result =  baseDAL.selectByCriteria(new String[]{"name","pwd"},queryCriteria, 60);
            List<Map<String, Object>> users = result.getList();
            //也可以转成对应的DTO列表
            List<User> users = result.asList(User.class);

	* 示例2

            //创建查询对象
			QueryCriteria queryCriteria = new QueryCriteria();
            //设置查询表，这样设置时类名必须与表名一致
        	queryCriteria.setTable(“user”);
            //设置分页
        	queryCriteria.setPageIndex(1);
        	queryCriteria.setPageSize(20);
            //设置排序
        	queryCriteria.setOrderByClause("age desc");
            //分页查询并缓存60称
        	QueryResult result =  baseDAL.selectPageByCriteria(queryCriteria, 60);
        	List<Map<String, Object>> list = result.getList();
        	Map<String, Object>  page = result.getPage();


* 主键查询

	* 示例1

			Map<String, Object> user = new HashMap<String, Object>();
        	user.put("id", 1);
            //缓存20秒
        	QueryResult result =  baseDAL.selectByPrimaryKey(user, 20);
            user = result.get();
            //也可以传为DTO对象
            User dto = result.as(User.class);

    * 示例2

			//显示部分字段，不使用缓存
	        QueryResult result =  baseDAL.selectByPrimaryKey(new String[]{"name","pwd"｝，"user", 1，BaseDal.NO_CAHCE);
            Map<String, Object> user = result.get();

    * 示例3

			//不使用DTO对象，直接传表名
	        QueryResult result =  baseDAL.selectByPrimaryKey(”user“, 1，BaseDal.NO_CAHCE);
            Map<String, Object> user = result.get();

    * 示例4

			//联合主键查询
			QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(“user”);
        	Criteria critera = queryCriteria.createCriteria();
            critera.andColumnEqualTo("name", "uncode-dal");
            critera.andColumnEqualTo("pwd", "uncode-dal-mybatis");
            queryCriteria.setSelectOne(true);
	        QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
            Map<String, Object> user = result.get();

* 插入
	* 示例1

			Map<String, Object> user = new HashMap<String, Object>();
        	user.put("name", "uncode");
            user.put("pwd", faster");
            //第一个参数为表名
        	int result = baseDAL.insert("user", user);

* 更新
	* 示例1

			Map<String, Object> user = new HashMap<String, Object>();
        	user.put("name", "uncode-dal-mybatis");
        	user.setName("uncode-dal-mybatis");
        	QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable("user");
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnEqualTo("name", "uncode");
        	int result = baseDAL.updateByCriteria(user, queryCriteria);

	* 示例2

			Map<String, Object> user = new HashMap<String, Object>();
        	user.put("email", "ywj_316@qq.com");
            user.put("id", 1);
            //第一个参数为表名，user中必须包括所有主键字段
        	int result = baseDAL.updateByPrimaryKey("user", user);

* 删除
	* 示例1

        	Map<String, Object> user = new HashMap<String, Object>();
        	user.put("email", "ywj_316@qq.com");
            user.put("id", 1);
            //user中必须包括所有主键字段
        	int result = baseDAL.deleteByPrimaryKey("user", user);

	* 示例2

            //第一个参数为表名
        	int result = baseDAL.deleteByPrimaryKey(“user”, 165);

	* 示例3

        	QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable("user");
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnEqualTo("name", "uncode");
        	int result = baseDAL.deleteByCriteria(queryCriteria);

* 其他操作
	* 示例1

			QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable("user");
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnGreaterThan("age", 18);
            critera.andColumnEqualTo("status", 1);
            //数量统计
        	int result =  baseDAL.countByCriteria(queryCriteria);

### 5 使用DTO示例

* 列表查询
	* 示例1

			//创建查询对象
			QueryCriteria queryCriteria = new QueryCriteria();
            //设置查询表，这样设置时类名必须与表名一致
        	queryCriteria.setTable(User.class);
            //创建条件封装对象
        	Criteria critera = queryCriteria.createCriteria();
            //设置条件
        	critera.andColumnGreaterThan(User.AGE, 18);
            critera.andColumnEqualTo(User.STATUS, 1);
            //查询所有字段并缓存
        	QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
            //查询部分字段不缓存
            QueryResult result =  baseDAL.selectByCriteria(new String[]{"name","pwd"},queryCriteria, BaseDal.NO_CACHE);
            List<User> users = result.asList(User.class);

	* 示例2

            //创建查询对象
			QueryCriteria queryCriteria = new QueryCriteria();
            //设置查询表，这样设置时类名必须与表名一致
        	queryCriteria.setTable(User.class);
            //设置分页
        	queryCriteria.setPageIndex(1);
        	queryCriteria.setPageSize(20);
            //设置排序
        	queryCriteria.setOrderByClause(User.AGE+" desc");
            //分页查询并缓存60称
        	QueryResult result =  baseDAL.selectPageByCriteria(queryCriteria, 60);
        	List<User> users = result.asList(User.class);
        	Map<String, Object>  page = result.getPage();


* 主键查询

	* 示例1

			User user = new User();
        	user.setId(1);
            //缓存20秒,有多个主键时必须全部设值
        	QueryResult result =  baseDAL.selectByPrimaryKey(user, 20);
            user = result.as(User.class);

    * 示例2

			//显示部分字段，不使用缓存
	        QueryResult result =  baseDAL.selectByPrimaryKey(new String[]{"name","pwd"｝，User.class, 1，BaseDal.NO_CAHCE);
            User user = result.as(User.class);

    * 示例3

			//联合主键也可以使用这种方式查询
			QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
            critera.andColumnEqualTo(User.NAME, "uncode-dal");
            critera.andColumnEqualTo(User.PWD, "uncode-dal-mybatis");
            queryCriteria.setSelectOne(true);
	        QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
			User user = result.as(User.class);

* 插入
	* 示例1

			User user = new User();
        	user.setName("uncode");
            user.setPwd("faster");
        	int result = baseDAL.insert(user);

* 更新
	* 示例1

			User user = new User();
        	user.setName("uncode-dal-mybatis");
        	QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnEqualTo(User.NAME, "uncode");
        	int result = baseDAL.updateByCriteria(user, queryCriteria);

	* 示例2

        	User user = new User();
        	user.setEmail("ywj_316@qq.com");
        	user.setId(1);
            //user中必须设值所有主键字段
        	int result = baseDAL.updateByPrimaryKey(user);

* 删除
	* 示例1

        	User user = new User();
        	user.setId(1);
            //user中必须设值所有主键字段
        	int result = baseDAL.deleteByPrimaryKey(user);

	* 示例2

        	int result = baseDAL.deleteByPrimaryKey(User.class, 165);

	* 示例3

        	QueryCriteria queryCriteria = new QueryCriteria();
        	queryCriteria.setTable(User.class);
        	Criteria critera = queryCriteria.createCriteria();
        	critera.andColumnEqualTo(User.NAME, "uncode");
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


