package cn.uncode.dal.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.uncode.dal.criteria.QueryCriteria;
import cn.uncode.dal.criteria.QueryCriteria.Criteria;
import cn.uncode.dal.descriptor.QueryResult;
import cn.uncode.dal.core.MongoDAL;

public class MongoTest {
    
    
    @Test
    public void testSelectByCriteria(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable("news");
        QueryResult result =  mongoDAL.selectByCriteria(queryCriteria);
        System.out.println(result.getList());
    }
    
    @Test
    public void testSelectByCriteriaIn(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable("news");
        List<Long> names = new ArrayList<Long>();
        names.add(0L);
        queryCriteria.createCriteria().andColumnIn("status", names);
        QueryResult result =  mongoDAL.selectByCriteria(queryCriteria);
        System.out.println(result.getList());
    }
    
    @Test
    public void testSelectByPrimaryKey(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
    	QueryResult result =  mongoDAL.selectByPrimaryKey("news", "564309048f12362c78956b38");
        System.out.println(result.get());
    }
    
    
    @Test
    public void testInsert(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("title", "test001236501685");
    	content.put("status", 0);
    	content.put("name", "123652");
    	Object result = mongoDAL.insert("news", content);
        System.out.println(result);
    }
    
    
    public static void main(String[] args) {
    	/*ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("title", "test001236501685");
    	content.put("status", 0);
    	content.put("name", "123652");
    	mongoDAL.asynInsert("news", content);*/
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable("expressinfo");
        Criteria criteria = queryCriteria.createCriteria();
        criteria.append("testflag", false).append("cityid", 1).append("{$or:[{nextsiteid:304200005354694},{nextsiteid:304158521636681,sendsiteid:304200005354694},{currentsiteid:304200005354694,nextsiteid:304200005354694}]}");
        QueryResult result =  mongoDAL.selectByCriteria(queryCriteria);
        System.out.println(result.getList());
	}
    
    
    
    @Test
    public void testDeleteByPrimaryKey(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("_id", "5642eadc8f12362dfcc2cd54");
        int result = mongoDAL.deleteByPrimaryKey("news", "5642eadc8f12362dfcc2cd54");
        System.out.println(result);
    }
    
    
    @Test
    public void testDeleteByCriteria(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable("news");
        Criteria critera = queryCriteria.createCriteria();
        critera.andColumnEqualTo("status", 1);
        int result = mongoDAL.deleteByCriteria(queryCriteria);
        System.out.println(result);
    }
    
    @Test
    public void testUpdateByCriteria(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("status", 1);
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable("news");
        Criteria critera = queryCriteria.createCriteria();
        critera.andColumnEqualTo("title", "test001236501");
        int result = mongoDAL.updateByCriteria(content, queryCriteria);
        System.out.println(result);
    }
    
    @Test
    public void testUpdateByPrimaryKey(){
    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application.xml" });
		context.start();
		MongoDAL mongoDAL = (MongoDAL) context.getBean("mongoDAL");
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("status", 2);
    	content.put("_id", "564309048f12362c78956b38");
        int result = mongoDAL.updateByPrimaryKey("news", content);
        System.out.println(result);
    }
    


}
