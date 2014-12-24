package org.fastser.dal.spring.jdbc;

import org.fastser.dal.core.BaseDAL;
import org.fastser.dal.criteria.QueryCriteria;
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
    public void testQuery(){
        QueryCriteria critera = new QueryCriteria();
        critera.setTable("user");
        QueryResult result =  baseDAL.selectByCriteria(critera);
        System.out.println(result.getList());
    }
    
    @Test
    public void test2(){
        String name = this.getClass().getName();
        System.out.println(firstToLower(name.substring(name.lastIndexOf(".")+1)));
    }
    
    public static String firstToLower(String str){
        char[] array = str.toCharArray();
        array[0] += 32;
        return String.valueOf(array);
    }


}
