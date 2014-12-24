package org.fastser.dal.spring.jdbc.model;

import java.io.Serializable;

public class ModifyParams implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -9165433475864049756L;

    private Object[] params;
    
    private int[] types;
    
    public ModifyParams(){
        params = new Object[0];
        types = new int[0];
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public int[] getTypes() {
        return types;
    }

    public void setTypes(int[] types) {
        this.types = types;
    }
    
    
    

}
