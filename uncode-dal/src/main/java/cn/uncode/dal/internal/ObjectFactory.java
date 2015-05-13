package cn.uncode.dal.internal;


import java.util.ArrayList;
import java.util.List;

import cn.uncode.dal.internal.util.message.Messages;



/**
 * This class creates the different objects needed by the generator
 * 
 */
public class ObjectFactory {
    private static List<ClassLoader> classLoaders;
    
    static {
    	classLoaders = new ArrayList<ClassLoader>();
    }

    /**
     * Utility class. No instances allowed
     */
    private ObjectFactory() {
        super();
    }

    /**
     * Adds a custom classloader to the collection of classloaders
     * searched for resources.  Currently, this is only used
     * when searching for properties files that may be
     * referenced in the configuration file. 
     * 
     * @param classLoader class loader
     */
    public static synchronized void addClassLoader(
            ClassLoader classLoader) {
        ObjectFactory.classLoaders.add(classLoader);
    }

    
    /**
     * This method returns a class loaded from the context classloader, or the
     * classloader supplied by a client. This is appropriate for JDBC drivers,
     * model root classes, etc. It is not appropriate for any class that extends
     * one of the supplied classes or interfaces.
     * 
     * @param type type
     * @return the Class loaded from the external classloader
     * @throws ClassNotFoundException class not found
     */
    public static Class<?> classForName(String type)
            throws ClassNotFoundException {

        Class<?> clazz;

        for (ClassLoader classLoader : classLoaders) {
            try {
                clazz = Class.forName(type, true, classLoader);
                return clazz;
            } catch (Throwable e) {
                // ignore - fail safe below
                ;
            }
        }
        
        return internalClassForName(type);
    }

    public static Object createObject(String type) {
        Object answer;

        try {
            Class<?> clazz = classForName(type);
            answer = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(Messages.getString("RuntimeError.0", type), e); 
        }

        return answer;
    }

    public static Class<?> internalClassForName(String type)
            throws ClassNotFoundException {
        Class<?> clazz = null;

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            clazz = Class.forName(type, true, cl);
        } catch (Exception e) {
            // ignore - failsafe below
        }

        if (clazz == null) {
            clazz = Class.forName(type, true, ObjectFactory.class.getClassLoader());
        }

        return clazz;
    }

}
