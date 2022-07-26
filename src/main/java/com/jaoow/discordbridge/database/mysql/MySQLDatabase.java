package com.jaoow.discordbridge.database.mysql;


import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.database.Database;

import java.sql.*;

public class MySQLDatabase extends Database {

    private final String databaseName;
    private Connection connection;

    protected String hostName;
    protected String userName;
    protected String password;
    protected int port;
    public MySQLDatabase(String hostName, String userName, String password, int port, String databaseName) {
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
        this.port = port;
        this.databaseName = databaseName;
    }

    public void tryConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            DiscordLink.getPluginLogger().warning("MySQL driver not found!");
        }

        this.connection = DriverManager.getConnection("jdbc:mysql://" + this.hostName + ":" + this.port + "/" + this.databaseName + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", this.userName, this.password);
    }

    public void close() {
        try {
            if (!this.getConnection().isClosed()) {
                this.getConnection().close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (this.connection != null && this.connection.isClosed()) {
                this.tryConnection();
            }
            if (this.connection == null) {
                this.tryConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.connection;
    }
}

