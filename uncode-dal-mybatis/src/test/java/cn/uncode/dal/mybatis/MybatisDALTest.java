package cn.uncode.dal.mybatis;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.uncode.dal.core.BaseDAL;
import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.criteria.QueryCriteria.Criteria;
import cn.uncode.dal.descriptor.QueryResult;
import cn.uncode.dal.utils.JsonUtils;
import cn.uncode.dal.mybatis.User;

public class MybatisDALTest {
	
	static ClassPathXmlApplicationContext context;
    
    private static BaseDAL baseDAL;
    
    @Test
    public void testSelectByCriteria(){
		startService();
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable(User.class);
        Criteria critera = queryCriteria.createCriteria();
        critera.andColumnGreaterThan(User.ID, 1);
        critera.andColumnLessThanOrEqualTo(User.ID, 10000);
        QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
        System.out.println(result.getList());
    }
    
    @Test
    public void testSelectByPrimaryKey1(){
    	startService();
		baseDAL = (BaseDAL) context.getBean("baseDAL");
        User user = new User();
        user.setId(124);
        QueryResult result =  baseDAL.selectByPrimaryKey(user);
        System.out.println(result.get());
    }
    @Test
    public void testSelectByPrimaryKey2(){
    	QueryResult result =  baseDAL.selectByPrimaryKey("user", 1);
        System.out.println(result.get());
    }
    @Test
    public void testSelectByPrimaryKey3(){
        QueryResult result =  baseDAL.selectByPrimaryKey(User.class, 1);
        System.out.println(result.get());
    }
    
    @Test
    public void testInsert1(){
    	startService();
        User user = new User();
        user.setName("test001236501");
        Object result = baseDAL.insert(user);
        Long id = (Long)result;
        System.out.println(result);
    }
    
    @Test
    public void testInsert2(){
    	startService();
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("nickname", "test001236501");
    	Object result = baseDAL.insert("user", content);
        System.out.println(result);
    }
    
   
    
   
    
    @Test
    public void testDeleteByPrimaryKey1(){
        User user = new User();
        user.setId(165);
        int result = baseDAL.deleteByPrimaryKey(user);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByPrimaryKey2(){
        int result = baseDAL.deleteByPrimaryKey(User.class, 165);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByPrimaryKey3(){
        int result = baseDAL.deleteByPrimaryKey("user", 165);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByPrimaryKey4(){
    	startService();
        int result = baseDAL.deleteByPrimaryKey("express", 165);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByCriteria(){
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable(User.class);
        Criteria critera = queryCriteria.createCriteria();
        critera.andColumnEqualTo(User.USER_NAME, "test001236501");
        int result = baseDAL.deleteByCriteria(queryCriteria);
        System.out.println(result);
    }
    
    @Test
    public void testUpdateByCriteria(){
        User user = new User();
        user.setEmail("test6@xiaocong.tv");
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable(User.class);
        Criteria critera = queryCriteria.createCriteria();
        critera.andColumnEqualTo(User.USER_NAME, "test001236501");
        int result = baseDAL.updateByCriteria(user, queryCriteria);
        System.out.println(result);
    }
    
    @Test
    public void testUpdateByPrimaryKey(){
        User user = new User();
        user.setEmail("test@xiaocong.tv");
        user.setId(163);
        int result = baseDAL.updateByPrimaryKey(user);
        System.out.println(result);
    }
    
    @Test
    public void testUpdateByPrimaryKey2(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 163);
        map.put("name", "test@xiaocong.tv");
        int result = baseDAL.updateByPrimaryKey("user",map);
        System.out.println(result);
    }
    
    @Test
    public void testMapToBean(){
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("usErnaMe", "123");
    	map.put("pwd", "333333333");
    	User user = JsonUtils.mapToObj(map, User.class);
        //System.out.println(user.getUserName()+"=="+user.getPwd());
    }
    
    
	public static void startService(){
		try {
			context = new ClassPathXmlApplicationContext(new String[] { "application.xml"});
			context.start();
			baseDAL = (BaseDAL) context.getBean("baseDAL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
