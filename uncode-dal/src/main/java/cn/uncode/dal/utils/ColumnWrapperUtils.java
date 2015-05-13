package cn.uncode.dal.utils;

public class ColumnWrapperUtils {

    private static final String KEY_WORDS = "index,key,value,table,option,fields,version,user,name,status,desc";

    public static String wrap(String column) {
        if (KEY_WORDS.indexOf(column) != -1) {
            return "`" + column + "`";
        } else {
            return column;
        }
    }

}

