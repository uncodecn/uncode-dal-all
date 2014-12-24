package org.fastser.dal.mybatis;

import java.util.HashMap;
import java.util.Map;

import org.fastser.dal.core.BaseDAL;
import org.fastser.dal.criteria.Model;
import org.fastser.dal.criteria.QueryCriteria;
import org.fastser.dal.criteria.QueryCriteria.Criteria;
import org.fastser.dal.descriptor.QueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application.xml")
public class QueryTest {
    
    @Autowired
    private BaseDAL baseDAL;
    
    @Test
    public void testSelectByCriteria(){
        QueryCriteria queryCriteria = new QueryCriteria();
        queryCriteria.setTable(User.class);
        Criteria critera = queryCriteria.createCriteria();
        critera.andColumnGreaterThan(User.ID, 1);
        QueryResult result =  baseDAL.selectByCriteria(queryCriteria);
        System.out.println(result.getList());
    }
    
    @Test
    public void testSelectByPrimaryKey1(){
        User user = new User();
        user.setId(1);
        QueryResult result =  baseDAL.selectByPrimaryKey(user);
        System.out.println(result.get());
    }
    @Test
    public void testSelectByPrimaryKey2(){
        Model model = new Model(User.class);
        model.setSinglePrimaryKey(1);
        QueryResult result =  baseDAL.selectByPrimaryKey(model);
        System.out.println(result.get());
    }
    @Test
    public void testSelectByPrimaryKey3(){
        QueryResult result =  baseDAL.selectByPrimaryKey(User.class, 1);
        System.out.println(result.get());
    }
    
    @Test
    public void testInsert1(){
    	Model model = new Model(User.class);
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put(User.USER_NAME, "test001236501");
    	model.addContent(content);
        int result = baseDAL.insert(model);
        System.out.println(result);
    }
    
    @Test
    public void testInsert2(){
        User user = new User();
        user.setUserName("test001236501");
        int result = baseDAL.insert(user);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByPrimaryKey1(){
        Model model = new Model(User.class);
        model.setSinglePrimaryKey(164);
        int result = baseDAL.deleteByPrimaryKey(model);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByPrimaryKey2(){
        User user = new User();
        user.setId(165);
        int result = baseDAL.deleteByPrimaryKey(user);
        System.out.println(result);
    }
    
    @Test
    public void testDeleteByPrimaryKey3(){
        int result = baseDAL.deleteByPrimaryKey(User.class, 165);
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
    


}
