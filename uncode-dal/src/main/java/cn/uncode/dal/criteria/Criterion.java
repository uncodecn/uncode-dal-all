package cn.uncode.dal.criteria;

import java.util.List;

public class Criterion {
    
    private String column;
    
    private Integer condition;
    
    private Object value;

    private Object secondValue;

    private boolean noValue;

    private boolean singleValue;

    private boolean betweenValue;

    private boolean listValue;

    private String typeHandler;

    public Integer getCondition() {
        return condition;
    }

    public Object getValue() {
        return value;
    }

    public Object getSecondValue() {
        return secondValue;
    }

    public boolean isNoValue() {
        return noValue;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public boolean isBetweenValue() {
        return betweenValue;
    }

    public boolean isListValue() {
        return listValue;
    }

    public String getTypeHandler() {
        return typeHandler;
    }
    
    public String getColumn() {
        return column;
    }


    public Criterion(Integer condition, Object value, String typeHandler, String column) {
        super();
        this.condition = condition;
        this.value = value;
        this.typeHandler = typeHandler;
        this.column = column;
        if (value instanceof List<?>) {
            this.listValue = true;
        } else {
            this.singleValue = true;
        }
        if(value == null){
            this.noValue = true;
        }
    }
    
    public Criterion(String sql) {
        this(null, null, null, sql);
    }
    
    public Criterion(Integer condition, String column) {
        this(condition, null, null, column);
    }

    public Criterion(Integer condition, Object value, String column) {
        this(condition, value, null, column);
    }

    public Criterion(Integer condition, Object value, Object secondValue, String typeHandler, String column) {
        super();
        this.condition = condition;
        this.value = value;
        this.secondValue = secondValue;
        this.typeHandler = typeHandler;
        this.betweenValue = true;
    }

    public Criterion(Integer condition, Object value, Object secondValue, String column) {
        this(condition, value, secondValue, null, column);
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((secondValue == null) ? 0 : secondValue.hashCode());
        result = prime * result + (noValue ? 1231 : 1237);
        result = prime * result + (singleValue ? 1231 : 1237);
        result = prime * result + (betweenValue ? 1231 : 1237);
        result = prime * result + (listValue ? 1231 : 1237);
        result = prime * result + ((typeHandler == null) ? 0 : typeHandler.hashCode());
        return result;
    }
    
    public static class Condition{
        
        public static final int IS_NULL = 1;
        public static final int IS_NOT_NULL = 2;
        public static final int EQUAL = 3;
        public static final int NOT_EQUAL = 4;
        public static final int GREATER_THAN = 5;
        public static final int GREATER_THAN_OR_EQUAL = 6;
        public static final int LESS_THAN = 7;
        public static final int LESS_THAN_OR_EQUAL = 8;
        public static final int LIKE = 9;
        public static final int NOT_LIKE = 10;
        public static final int IN = 11;
        public static final int NOT_IN = 12;
        public static final int BETWEEN = 13;
        public static final int NOT_BETWEEN = 14;
       
    }

}
