package com.datatactics.l3.fcc.atlas.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {

    private final static String MYSQL_CLASSNAME = "com.mysql.jdbc.Driver";
    private final static String MYSQL_DEFAULT_USER = "fccuser";
    private final static String MYSQL_DEFAULT_USER_PASSWORD = "fcc2014!";
    private final static String MYSQL_SERVER = "localhost";
    private final static String MYSQL_PORT = "3306";
    private final static String MYSQL_SERLVET_DATABASE = "servlets";
    
    private static Connector instance = null;
    private static Connection connection = null;
    
    public Connector(String user, String password, String server, String port, String database) throws SQLException {
        
        if (classVerified()) {
            System.out.println("Driver found");

            try {
                /*
                String user =       context.getInitParameter("mysql.user");
                String password =   context.getInitParameter("mysql.password");
                String server =     context.getInitParameter("mysql.server");
                String port =       context.getInitParameter("mysql.port");
                String database =   context.getInitParameter("mysql.database");
                */
                
                String jdbcUrl = createJDBCUrl(server, port, database);
                
                System.out.println("connecting to " + jdbcUrl);
                connection = DriverManager.getConnection(jdbcUrl, user, password);
                
                System.out.println("Connection established");
            } catch(SQLException sqle) {
                sqle.printStackTrace();
                System.out.println("Connection failed");
                throw sqle;
            }
            
        }
    }
    
    private static String createJDBCUrl(String server, String port, String database) {
        StringBuilder builder = new StringBuilder();
        
        builder.append("jdbc:mysql://");
        builder.append(server);
        builder.append(":");
        builder.append(port);
        builder.append("/");
        builder.append(database);
        
        return builder.toString();
    }
    
    private static boolean classVerified() {
        boolean retVal = false;
        
        try {
            Class.forName(MYSQL_CLASSNAME);
            retVal = true;            
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            System.out.println("Driver not found");
        }
        
        return retVal;
    }
    
    public Connection getConnection() {
        return connection;
    }
}
