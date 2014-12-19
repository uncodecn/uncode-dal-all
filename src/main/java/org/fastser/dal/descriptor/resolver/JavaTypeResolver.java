/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.fastser.dal.descriptor.resolver;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author wj.ye
 */
public class JavaTypeResolver {

    private static final Map<Integer, JavaType> typeMap = new HashMap<Integer, JavaType>();;

    public JavaTypeResolver() {
        super();
        init();
    }

    private static void init() {
        typeMap.put(Types.ARRAY, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.BIGINT, JavaType.forType(Long.class.getName()));
        typeMap.put(Types.BINARY, JavaType.forType(Byte.class.getName()));
        typeMap.put(Types.BIT, JavaType.forType(Boolean.class.getName()));
        typeMap.put(Types.BLOB, JavaType.forType(Byte.class.getName()));
        typeMap.put(Types.BOOLEAN, JavaType.forType(Boolean.class.getName()));
        typeMap.put(Types.CHAR, JavaType.forType(String.class.getName()));
        typeMap.put(Types.CLOB, JavaType.forType(String.class.getName()));
        typeMap.put(Types.DATALINK, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.DATE, JavaType.forType(Date.class.getName()));
        typeMap.put(Types.DISTINCT, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.DOUBLE, JavaType.forType(Double.class.getName()));
        typeMap.put(Types.FLOAT, JavaType.forType(Double.class.getName()));
        typeMap.put(Types.INTEGER, JavaType.forType(Integer.class.getName()));
        typeMap.put(Types.JAVA_OBJECT, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.LONGVARBINARY, JavaType.forType(Byte.class.getName()));
        typeMap.put(Types.LONGVARCHAR, JavaType.forType(String.class.getName()));
        typeMap.put(Types.NULL, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.OTHER, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.REAL, JavaType.forType(Float.class.getName()));
        typeMap.put(Types.REF, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.SMALLINT, JavaType.forType(Integer.class.getName()));
        typeMap.put(Types.STRUCT, JavaType.forType(Object.class.getName()));
        typeMap.put(Types.TIME, JavaType.forType(Date.class.getName()));
        typeMap.put(Types.TIMESTAMP, JavaType.forType(Date.class.getName()));
        typeMap.put(Types.TINYINT, JavaType.forType(Byte.class.getName()));
        typeMap.put(Types.VARBINARY, JavaType.forType(Byte.class.getName()));
        typeMap.put(Types.VARCHAR, JavaType.forType(String.class.getName()));
    }

    public static JavaType calculateJavaType(int jdbcType) {
        if (typeMap != null && typeMap.size() == 0) {
            init();
        }
        JavaType type = typeMap.get(jdbcType);

        if (type == null) {
            switch (jdbcType) {
            case Types.DECIMAL:
            case Types.NUMERIC:
            }
        }
        return type;
    }

}
