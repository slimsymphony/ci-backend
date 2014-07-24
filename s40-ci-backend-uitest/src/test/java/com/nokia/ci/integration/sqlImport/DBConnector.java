package com.nokia.ci.integration.sqlImport;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 1/16/13
 * Time: 9:52 AM
 * To change this template use File | Settings | File Templates.
 */


import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DBConnector {

    private static final String DRIVER_NAME = System.getProperty("driver", "org.h2.Driver");
    private static final String USER = System.getProperty("user", "test");
    private static final String PASSWORD = System.getProperty("password", "test");
    private static final String URL = System.getProperty("testDBJdbcUrl", "jdbc:h2:tcp://localhost:9020/mem:testDB");


    static {
        try {
            Class.forName(DRIVER_NAME).newInstance();
            System.out.println("*** Driver loaded");
        } catch (Exception e) {
            System.out.println("*** Error : " + e.toString());
            System.out.println("*** ");
            System.out.println("*** Error : ");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void executeSql(List<String> sqlCommands) throws SQLException {
        Connection c = DBConnector.getConnection();
        Statement st = c.createStatement();
        for (String sqlCommand : sqlCommands) {
            st.execute(sqlCommand);
        }
    }
        
    public static ResultSet getSQLResults(String query) throws SQLException {
        Connection c = DBConnector.getConnection();
        Statement st = c.createStatement();
        return st.executeQuery(query);
    }

    public static List<String> getSqlCommandsFromFile(String resourcePath, boolean enableBlockMode) {
        List<String> sqlCommands = new ArrayList<String>();
        try {
            InputStream resourceAsStream = DBConnector.class.getResourceAsStream(resourcePath);
            Reader fr = new InputStreamReader(resourceAsStream);
            BufferedReader br = new BufferedReader(fr);

            if (enableBlockMode) {
                String fileContentAsString = IOUtils.toString(br);
                sqlCommands.add(fileContentAsString);
                return sqlCommands;
            } else {
                return readCommandByCommand(br);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sqlCommands;
    }
    
    public static List<String> getColumnAsStringList(String query, String column) {
        List<String> results = new ArrayList<String>();
        try {
            ResultSet rs = getSQLResults(query);
            while (rs.next()) {
                results.add(rs.getString(column));
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }        
        return results;
    }
    
    public static List<String> getColumnAsStringList(String query, Integer column) {
        List<String> results = new ArrayList<String>();
        try {
            ResultSet rs = getSQLResults(query);
            while (rs.next()) {
                results.add(rs.getString(column));
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }        
        return results;
    }
    
    public static List<Integer> getColumnAsIntegerList(String query, Integer column) {
        List<Integer> results = new ArrayList<Integer>();
        try {
            ResultSet rs = getSQLResults(query);
            while (rs.next()) {
                results.add(rs.getInt(column));
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }        
        return results;
    }
    
    public static List<Integer> getColumnAsIntegerList(String query, String column) {
        List<Integer> results = new ArrayList<Integer>();
        try {
            ResultSet rs = getSQLResults(query);
            while (rs.next()) {
                results.add(rs.getInt(column));
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }        
        return results;
    }

    private static List<String> readCommandByCommand(BufferedReader br) throws IOException {
        final String delimiter = ";";
        List<String> sqlCommands = new ArrayList<String>();
        int overflow = SqlSplitter.NO_END;
        StringBuffer sqlCommand = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("--")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line);
            if (st.hasMoreTokens()) {
                String token = st.nextToken();
                if ("REM".equalsIgnoreCase(token)) {
                    continue;
                }
            }
            sqlCommand.append(" ").append(line);

            overflow = SqlSplitter.containsSqlEnd(line, delimiter, overflow);
            if (overflow == SqlSplitter.NO_END) {
                sqlCommand.append("\n");
            }
            // SQL defines "--" as a comment to EOL
            // and in Oracle it may contain a hint
            // so we cannot just remove it, instead we must end it
            if (overflow > 0) {
                sqlCommands.add(sqlCommand.toString());
                sqlCommand.setLength(0); // clean buffer
                overflow = SqlSplitter.NO_END;
            }
        }
        // Catch any statements not followed by ;
        if (sqlCommand.length() > 0) {
            sqlCommands.add(sqlCommand.toString());
        }
        return sqlCommands;
    }
}