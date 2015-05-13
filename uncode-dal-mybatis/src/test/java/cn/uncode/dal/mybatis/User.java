package cn.uncode.dal.mybatis;

import java.io.Serializable;

public class User implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4799201163494761002L;
    
    public static final String ID = "id";
    public static final String USER_NAME = "userName";
    public static final String PWD = "pwd";
    public static final String EMAIL = "email";
    public static final String USER_STATUS = "userStatus";
    
    private Integer id;

    private String name;

    private String pwd;

    private String email;

    private Integer version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

    
    
    

}
