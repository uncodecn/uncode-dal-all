package cn.uncode.dal.spring.jdbc;

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

    private String userName;

    private String pwd;

    private String email;

    private Integer userStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }
    
    

}
