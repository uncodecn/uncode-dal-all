package cn.uncode.dal.descriptor.resolver;

import java.util.HashMap;
import java.util.Map;

public enum JavaType {
    
    BOOLEAN("java.lang.Boolean"),
    CHARACTER("java.lang.Character"),
    DOUBLE("java.lang.Double"),
    FLOAT("java.lang.Float"),
    INTEGER("java.lang.Integer"),
    LONG("java.lang.Long"),
    STRING("java.lang.String"),
    SHORT("java.lang.Short"),
    DATE("java.util.Date"),
    BYTE("java.lang.Byte"),
    OBJECT("java.lang.Object");

    public final String TYPE;
    
    private static Map<String,JavaType> codeLookup = new HashMap<String,JavaType>();

    static {
      for (JavaType type : JavaType.values()) {
        codeLookup.put(type.TYPE, type);
      }
    }

    JavaType(String type) {
      this.TYPE = type;
    }

    public static JavaType forType(String type)  {
      return codeLookup.get(type);
    }
    
    public String value(){
        return TYPE;
    }
}
