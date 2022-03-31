package com.app.cloudstorage.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class Database {

    private static final Logger logger = LogManager.getLogger(Database.class);
    private static Connection connection;
    private static Statement stmt;

    public static void connect(){
        try{
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try{
            connection = DriverManager.getConnection("jdbc:sqlite::resource:users.db");
            stmt = connection.createStatement();
        }catch (SQLException e){
            logger.catching(e);
        }
    }

    public static void disconnect(){
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGoodConnectUser(String login, String pass){
        try{
            String query = "SELECT login FROM main WHERE login = ? AND password = ?;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                logger.debug("User " + login + " подключился");
                return true;
            }
        }catch (Exception e){
           logger.catching(e);
        }
        logger.debug("Ошибка проверки логина " + login);
        return false;
    }
}
