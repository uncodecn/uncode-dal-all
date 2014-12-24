package org.fastser.dal.mybatis.service.impl;

import java.util.List;
import java.util.Map;

import org.fastser.dal.core.BaseDAL;
import org.fastser.dal.criteria.QueryCriteria;
import org.fastser.dal.mybatis.service.IUserServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplTest implements IUserServiceTest{
    
    @Autowired
    private BaseDAL baseDAL;

    @Override
    public List<Map<String, Object>> queryAll() {
        QueryCriteria critera = new QueryCriteria();
        critera.setTable("user");
        
        return null;
    }

}
