package com.jaoow.discordbridge.database;

import com.jaoow.discordbridge.database.querys.Query;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
public abstract class Database {


    public abstract void tryConnection() throws SQLException;

    public abstract void close() throws SQLException;

    public abstract Connection getConnection();

    public ResultSet executeQuery(Query query) {
        try {
            PreparedStatement statement = query.generate();
            return statement.executeQuery();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean executeUpdate(Query query) {
        try (PreparedStatement statement = query.generate()) {
            statement.execute();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}

