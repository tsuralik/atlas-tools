package com.datatactics.l3.fcc.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogFormatter {

    private static final Object EMPTY_STRING = "";
    private static final Object NEW_LINE = System.lineSeparator();
    private static final Object EXCEPTION_LOG_START =   "======================= exception log start =======================";
    private static final Object EXCEPTION_START =       "====================== exception trace start ======================";
    private static final Object EXCEPTION_END =         "======================= exception trace end =======================";
    private static final Object EXCEPTION_LOG_END =     "======================== exception log end ========================";
    private static final Object DEMARCATION =           "===================================================================";

    /**
     * Returns a string similar to the following (where "Caught exception" is the header parameter):
     * <p>
     * <code>
     * Caught exception:<p>
     * ===============<p>
     * exception start<p>
     * ===============<p>
     * <u><i>stack trace here</i></u><p>
     * ===============<p>
     * exception end<p>
     * ===============<p>
     * </code>
     * 
     * @param header
     * @param footer
     * @param e
     * @return
     */
    public static String formatException(String header, String footer, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        
        StringBuilder builder = new StringBuilder();
        
        addLogStart(builder);
        
        if (validString(header)) {
            addDemarcation(builder);
            builder.append(header);
        }

        addDemarcation(builder);
        builder.append(EXCEPTION_START);
        addDemarcation(builder);
        builder.append(sw.toString());
        addDemarcation(builder);
        builder.append(EXCEPTION_END);
        addDemarcation(builder);
        
        if (validString(footer)) {
            builder.append(footer);
            addDemarcation(builder);
        }
        
        addLogEnd(builder);
        
        return builder.toString();
    }

    private static void addLogStart(StringBuilder builder) {
        builder.append(NEW_LINE);
        builder.append(EXCEPTION_LOG_START);
        builder.append(NEW_LINE);
    }

    private static void addLogEnd(StringBuilder builder) {
        builder.append(NEW_LINE);
        builder.append(EXCEPTION_LOG_END);
        builder.append(NEW_LINE);
    }

    private static void addDemarcation(StringBuilder builder) {
        builder.append(NEW_LINE);
        builder.append(DEMARCATION);
        builder.append(NEW_LINE);
    }

    /**
     * Returns a string similar to the following (where "Caught exception" is the header parameter):
     * <p>
     * <code>
     * Caught exception:<p>
     * ===============<p>
     * exception start<p>
     * ===============<p>
     * <u><i>stack trace here</i></u><p>
     * ===============<p>
     * exception end<p>
     * ===============<p>
     * </code>
     * 
     * @param header
     * @param footer
     * @param e
     * @return
     */
    public static String defaultFormatException(Exception e) {
        return formatException("caught exception", null, e);
    }

    /** 
     * Check if string parameter is null or empty.
     * </p>
     * This method will NOT use the string trim() method to remove whitespace from the parameter 
     * in case the intention is to simply use extra white space.
     *  
     * @param str the string instance to be inspected
     * @return <code>true</code> if the string is not null and not empty, <code>false</code> otherwise
     */
    private static boolean validString(String str) {
        return ((str != null) && (!str.trim().equals(EMPTY_STRING)));
    }
}
