/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.wintec.service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class for logging
 *
 * @author mike
 */
public class Logger {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
    
    /**
     * logs a message
     *
     * @param caption
     * @param message
     */
    public static void log(Object caption, Object message) {
//        System.out.println(caption + "\t>\t" + message);
    }

    /**
     * logs an error message
     *
     * @param caption
     * @param message
     */
    public static void error(Object caption, Object message) {
//        System.err.println(caption + "\t>\t" + message);
    }

    /**
     * logs a fatal error message
     *
     * @param caption
     * @param message
     */
    public static void fatal(Object caption, Object message) {
        System.err.println(getTime() + "\t" + caption + "\t" + message);
    }
    
    /**
     * 
     * @return formatted current time
     */
    private static String getTime() {
        return dateFormat.format(new Date());
    }
}
