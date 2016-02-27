package cn.uncode.dal.criteria;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.uncode.dal.criteria.Criterion.Condition;


public class QueryCriteria {
	
	private String database;
    
    private String table;
    
    private String orderByClause;

    private String groupBy;
    
    private boolean distinct;

    private List<Criteria> oredCriteria;

    private boolean selectOne;
    
    private int pageIndex = 1;
    
    private int pageSize = 15;
    
    private int recordIndex = 0;
    
    private int limit;
    
    private Object version;
    
    public QueryCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }
    
    public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean getDistinct() {
        return distinct;
    }
    
    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
    	
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table.toLowerCase().trim();
    }
    
    public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public Object getVersion() {
		return version;
	}

	public void setVersion(Object version) {
		this.version = version;
	}
	
	public int getRecordIndex() {
		return recordIndex;
	}

	public void setRecordIndex(int recordIndex) {
		this.recordIndex = recordIndex;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setTable(Class<?> clazz) {
        String name = clazz.getName();
        name = name.substring(name.lastIndexOf(".")+1);
        char[] array = name.toCharArray();
        array[0] += 32;
        this.table = String.valueOf(array).toLowerCase().trim();
    }
    
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public void setSelectOne(boolean selectOne) {
        this.selectOne = selectOne;
    }

    public boolean getSelectOne() {
        return this.selectOne;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderByClause == null) ? 0 : orderByClause.hashCode());
        result = prime * result + ((database == null) ? 0 : database.hashCode());
        result = prime * result + ((table == null) ? 0 : table.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((groupBy == null) ? 0 : groupBy.hashCode());
        result = prime * result + (distinct ? 1231 : 1237);
        result = prime * result + (selectOne ? 1231 : 1237);
        result = prime * result + pageIndex + pageSize;
        result = prime * result + recordIndex + limit;
        if (oredCriteria != null) {
            for (Criteria criteria : oredCriteria) {
                for (Criterion cter : criteria.getAllCriteria()) {
                	result = prime * result + ((cter== null)?0:cter.hashCode());
                }
            }
        }
        return result;
    }
    
    public String toString(){
    	StringBuffer sb = new StringBuffer();
    	if(StringUtils.isNotEmpty(database)){
    		sb.append("database:").append(database).append(",");
    	}
    	if(StringUtils.isNotEmpty(table)){
    		sb.append("table:").append(table).append(",");
    	}
    	if(StringUtils.isNotEmpty(orderByClause)){
    		sb.append("orderByClause:").append(orderByClause).append(",");
    	}
    	if(StringUtils.isNotEmpty(groupBy)){
    		sb.append("groupBy:").append(groupBy).append(",");
    	}
    	sb.append("distinct:").append(distinct).append(",");
    	if(null != oredCriteria){
    		sb.append("oredCriteria:").append(oredCriteria).append(",");
    	}
    	sb.append("selectOne:").append(selectOne).append(",");
    	sb.append("pageIndex:").append(pageIndex).append(",");
    	sb.append("pageSize:").append(pageSize).append(",");
    	sb.append("recordIndex:").append(recordIndex).append(",");
    	if(null != version){
    		sb.append("version:").append(version).append(",");
    	}
    	sb.deleteCharAt(sb.lastIndexOf(","));
        
    	return sb.toString();
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }
        
        protected void addCriterion(String sql) {
            if (StringUtils.isEmpty(sql)) {
                throw new RuntimeException("Sql cannot be null");
            }
            criteria.add(new Criterion(sql));
        }
        
        protected void addCriterion(Condition condition, String column) {
            if (condition == null || StringUtils.isEmpty(column)) {
                throw new RuntimeException("Column for condition cannot be null");
            }
            criteria.add(new Criterion(condition, column));
        }
        
        protected void addCriterion(Condition condition, Object value, String typeHandler, String column) {
            if (value == null || condition == null || StringUtils.isEmpty(column)) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition, value, typeHandler, column));
        }

        protected void addCriterion(Condition condition, Object value1, Object value2, String typeHandler, String column) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + column + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2, typeHandler, column));
        }
        
        public Criteria andColumnSql(String sql) {
            addCriterion(sql);
            return (Criteria) this;
        }

        public Criteria andColumnIsNull(String column) {
            addCriterion(Condition.IS_NULL, column);
            return (Criteria) this;
        }

        public Criteria andColumnIsNotNull(String column) {
            addCriterion(Condition.IS_NOT_NULL, column);
            return (Criteria) this;
        }

        public Criteria andColumnEqualTo(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.EQUAL, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnNotEqualTo(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.NOT_EQUAL, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnGreaterThan(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.GREATER_THAN, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnGreaterThanOrEqualTo(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.GREATER_THAN_OR_EQUAL, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnLessThan(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.LESS_THAN, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnLessThanOrEqualTo(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.LESS_THAN_OR_EQUAL, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnIn(String column, List<?> values) {
        	if(null != values && values.size() > 0){
        		addCriterion(Condition.IN, values, values.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnNotIn(String column, List<Object> values) {
        	if(null != values && values.size() > 0){
        		addCriterion(Condition.NOT_IN, values, values.getClass().getName(), column);
        	}
            return (Criteria) this;
        }
        
        public Criteria andColumnLike(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.LIKE, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }
        
        public Criteria andColumnNotLike(String column, Object value) {
        	if(null != value){
        		addCriterion(Condition.NOT_LIKE, value, value.getClass().getName(), column);
        	}
            return (Criteria) this;
        }

        public Criteria andColumnBetween(String column, Object value1, Object value2) {
            addCriterion(Condition.BETWEEN, value1, value2, value1.getClass().getName(), column);
            return (Criteria) this;
        }

        public Criteria andColumnNotBetween(String column, Object value1, Object value2) {
            addCriterion(Condition.NOT_BETWEEN, value1, value2, value1.getClass().getName(), column);
            return (Criteria) this;
        }

    }

    public class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
        /**
         * 快速封装查询条件
         * @param condition Condition条件枚举
         * @param filed 字段
         * @param value 值
         * @return
         */
        public Criteria append(Condition condition, String filed, Object value){
        	if(condition == Condition.EQUAL){
        		andColumnEqualTo(filed, value);
    		}else if(condition == Condition.NOT_EQUAL){
    			andColumnNotEqualTo(filed, value);
    		}else if(condition == Condition.GREATER_THAN){
    			andColumnGreaterThan(filed, value);
    		}else if(condition == Condition.GREATER_THAN_OR_EQUAL){
    			andColumnGreaterThanOrEqualTo(filed, value);
    		}else if(condition == Condition.LESS_THAN){
    			andColumnLessThan(filed, value);
    		}else if(condition == Condition.LESS_THAN_OR_EQUAL){
    			andColumnLessThanOrEqualTo(filed, value);
    		}else if(condition == Condition.LIKE){
    			andColumnLike(filed, value);
    		}else if(condition == Condition.SQL){
    			andColumnSql(value.toString());
    		}else{
    			andColumnEqualTo(filed, value);
    		}
        	return this;
        }
        
        /**
         * 快速封装查询条件
         * @param filed 字段
         * @param value 值
         * @return
         */
        public Criteria append(String filed, Object value){
        	return append(Condition.EQUAL, filed, value);
        }
        
        /**
         * 快速封装sql查询条件
         * @param sql sql片段
         * @return
         */
        public Criteria append(String sql){
        	return append(Condition.SQL, null, sql);
        }
        
        public String toString(){
        	if(null != criteria){
        		return criteria.toString();
        	}
        	return null;
        }
    }
    
    
    

}