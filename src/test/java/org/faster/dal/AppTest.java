package org.faster.dal;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        //assertTrue( true );
    	String sql = "jdbc:mysql://172.16.30.98:3306/rest?characterEncoding=utf8";
    	sql = sql.substring(0, sql.indexOf("?"));
    	sql = sql.substring(sql.lastIndexOf("/")+1, sql.length());
    	System.out.println(sql);
    }
}
