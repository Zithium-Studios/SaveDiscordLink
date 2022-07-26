package com.jaoow.discordbridge.database.mysql;

import com.jaoow.discordbridge.database.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase extends Database {

    protected Connection connection;

    public SQLiteDatabase(String filepath) throws SQLException {
        String url = String.format("jdbc:sqlite:%s", filepath);
        connection = DriverManager.getConnection(url);
    }

    @Override
    public void tryConnection() throws SQLException {

    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
