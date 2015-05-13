package cn.uncode.dal.spring.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import cn.uncode.dal.descriptor.Table;
import cn.uncode.dal.descriptor.resolver.JavaType;
import cn.uncode.dal.descriptor.resolver.JavaTypeConversion;
import cn.uncode.dal.descriptor.resolver.JavaTypeResolver;
import cn.uncode.dal.spring.jdbc.model.ModifyParams;
import cn.uncode.dal.spring.jdbc.template.SqlTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class CommonJdbcSupport extends JdbcDaoSupport {

    
    public List<Map<String, Object>> selectByCriteria(final Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.selectByCriteria(table);
        return getJdbcTemplate().queryForList(sql, buildParameters(table.getConditions()));
    }

    private Object[] buildParameters(LinkedHashMap<String, Object> params) {
        Object[] objs = null;
        if (params != null && params.size() > 0) {
            objs = new Object[params.size()];
            Iterator<Object> values = params.values().iterator();
            int index = 0;
            while (values.hasNext()) {
                objs[index] = values.next();
                index++;
            }
        }
        return objs;
    }

    private Object[] buildPrimaryKeyParameters(final Table table) {
        Object[] objs = null;
        LinkedHashMap<String, Object> params = table.getConditions();
        List<String> names = table.getPrimaryKey().getFields();
        if (names != null) {
            objs = new Object[names.size()];
            for (int i = 0; i < names.size(); i++) {
                objs[i] = params.get(names.get(i));
            }
        }
        return objs;
    }

    public int countByCriteria(final Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.countByCriteria(table);
        return getJdbcTemplate().queryForInt(sql, buildParameters(table.getConditions()));
    }



    public Map<String, Object> selectByPrimaryKey(final Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.selectByPrimaryKey(table);
        return getJdbcTemplate().queryForMap(sql, buildPrimaryKeyParameters(table));
    }


    public int insert(Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.insert(table);
        final ModifyParams modifyParams = buildInsertParameters(table);
        // int rs = this.getJdbcTemplate().update(sql, modifyParams.getParams(),
        // modifyParams.getTypes());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < modifyParams.getParams().length; i++) {
                    JavaType type = JavaTypeResolver.calculateJavaType(modifyParams.getTypes()[i]);
                    String value = String.valueOf(modifyParams.getParams()[i]);
                    if (JavaType.STRING == type) {
                        ps.setString(i + 1, value);
                    } else if (JavaType.BOOLEAN == type) {
                        ps.setBoolean(i + 1, Boolean.valueOf(value));
                    } else if (JavaType.BYTE == type) {
                        ps.setString(i + 1, value);
                    } else if (JavaType.CHARACTER == type) {
                        ps.setString(i + 1, value);
                    } else if (JavaType.DATE == type) {
                    	ps.setTimestamp(i + 1, JavaTypeConversion.convert(JavaType.DATE, value, Timestamp.class));
                    } else if (JavaType.DOUBLE == type) {
                        ps.setDouble(i + 1, Double.valueOf(value));
                    } else if (JavaType.FLOAT == type) {
                        ps.setFloat(i + 1, Float.valueOf(value));
                    } else if (JavaType.INTEGER == type) {
                        String[] vals = value.split("\\.");
                        ps.setInt(i + 1, Integer.valueOf(vals[0]));
                    } else if (JavaType.LONG == type) {
                        ps.setLong(i + 1, Long.valueOf(value));
                    } else if (JavaType.SHORT == type) {
                        ps.setShort(i + 1, Short.valueOf(value));
                    }
                }
                return ps;
            }
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    private ModifyParams buildInsertParameters(Table table) {
        ModifyParams modifyParams = null;
        Object[] params = null;
        int[] types = null;
        LinkedHashMap<String, Object> paramsMap = table.getParams();
        if (paramsMap != null) {
            modifyParams = new ModifyParams();
            params = new Object[paramsMap.size()];
            types = new int[paramsMap.size()];
            Iterator<Entry<String, Object>> iterator = paramsMap.entrySet().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                if (entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())) {
                    params[index] = paramsMap.get(key);
                    types[index] = table.getField(key).getJdbcType();
                    index++;
                }
            }
            int[] newTypes = Arrays.copyOf(types, index);
            Object[] newParams = new Object[index];
            System.arraycopy(params, 0, newParams, 0, index);
            modifyParams.setParams(newParams);
            modifyParams.setTypes(newTypes);
        }
        return modifyParams;
    }
    


    public int updateByCriteria(Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.updateByCriteria(table);
        ModifyParams modifyParams = buildUpdateParameters(table);
        int rs = this.getJdbcTemplate().update(sql, modifyParams.getParams(), modifyParams.getTypes());
        return rs;
    }

    private ModifyParams buildUpdateParameters(Table table) {
        ModifyParams modifyParams = null;
        Object[] params = null;
        int[] types = null;
        LinkedHashMap<String, Object> paramsMap = table.getParams();
        LinkedHashMap<String, Object> conditionsMap = table.getConditions();
        int index = 0;
        modifyParams = new ModifyParams();
        if (paramsMap != null) {
            params = new Object[paramsMap.size()];
            types = new int[paramsMap.size()];
            Iterator<String> iter = paramsMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                params[index] = paramsMap.get(key);
                types[index] = table.getField(key).getJdbcType();
                index++;
            }
        }
        if (conditionsMap != null) {
            Iterator<String> iter = conditionsMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                params[index] = conditionsMap.get(key);
                types[index] = table.getField(key).getJdbcType();
                index++;
            }
        }
        modifyParams.setParams(params);
        modifyParams.setTypes(types);
        return modifyParams;
    }


    public int updateByPrimaryKey(Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.updateByPrimaryKey(table);
        ModifyParams modifyParams = buildUpdateByPrimaryKey(table);

        int rs = this.getJdbcTemplate().update(sql, modifyParams.getParams(), modifyParams.getTypes());
        return rs;
    }

    private ModifyParams buildUpdateByPrimaryKey(Table table) {
        ModifyParams modifyParams = null;
        Object[] params = null;
        int[] types = null;
        LinkedHashMap<String, Object> paramsMap = table.getParams();
        List<String> names = table.getPrimaryKey().getFields();
        int size = 0;
        int index = 0;
        if (names != null) {
            size += names.size();
        }
        if (paramsMap != null) {
            size += paramsMap.size();
        }
        modifyParams = new ModifyParams();
        if (paramsMap != null) {
            params = new Object[size];
            types = new int[size];
            Iterator<String> iter = paramsMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();

                types[index] = table.getField(key).getJdbcType();
                JavaType javaType = JavaTypeResolver.calculateJavaType(table.getField(key).getJdbcType());

                if (javaType == JavaType.DATE) {
                    params[index] = JavaTypeConversion.convert(JavaType.DATE, paramsMap.get(key));
                } else if (JavaType.INTEGER == javaType || "INTEGER".equals(javaType)) {
                    params[index] = paramsMap.get(key).toString().split("\\.")[0];
                } else {
                    params[index] = paramsMap.get(key);
                }
                index++;
            }
        }
        if (names != null) {
            for (String name : names) {
                params[index] = table.getConditions().get(name);
                types[index] = table.getField(name).getJdbcType();
                index++;
            }
        }
        modifyParams.setParams(params);
        modifyParams.setTypes(types);
        return modifyParams;
    }


    public int deleteByPrimaryKey(Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.deleteByPrimaryKey(table);
        ModifyParams modifyParams = buildDeleteByPrimaryKey(table);
        int rs = this.getJdbcTemplate().update(sql, modifyParams.getParams(), modifyParams.getTypes());
        return rs;
    }

    private ModifyParams buildDeleteByPrimaryKey(Table table) {
        ModifyParams modifyParams = null;
        Object[] params = null;
        int[] types = null;
        List<String> names = table.getPrimaryKey().getFields();
        if (names != null) {
            modifyParams = new ModifyParams();
            params = new Object[names.size()];
            types = new int[names.size()];
            int index = 0;
            for (String name : names) {
                params[index] = table.getConditions().get(name);
                types[index] = table.getField(name).getJdbcType();
                index++;
            }
            modifyParams.setParams(params);
            modifyParams.setTypes(types);
        }

        return modifyParams;
    }


    public int deleteByCriteria(Table table) {
        SqlTemplate sqlTemplate = new SqlTemplate();
        final String sql = sqlTemplate.deleteByCriteria(table);
        ModifyParams modifyParams = buildDeleteByCriteria(table);
        int rs = this.getJdbcTemplate().update(sql, modifyParams.getParams(), modifyParams.getTypes());
        return rs;
    }

    private ModifyParams buildDeleteByCriteria(Table table) {
        ModifyParams modifyParams = null;
        Object[] params = null;
        int[] types = null;
        LinkedHashMap<String, Object> conditionsMap = table.getConditions();
        if (conditionsMap != null) {
            modifyParams = new ModifyParams();
            params = new Object[conditionsMap.size()];
            types = new int[conditionsMap.size()];
            Iterator<String> iter = conditionsMap.keySet().iterator();
            int index = 0;
            while (iter.hasNext()) {
                String key = iter.next();
                params[index] = conditionsMap.get(key);
                types[index] = table.getField(key).getJdbcType();
                index++;
            }
            modifyParams.setParams(params);
            modifyParams.setTypes(types);
        }

        return modifyParams;
    }




}
