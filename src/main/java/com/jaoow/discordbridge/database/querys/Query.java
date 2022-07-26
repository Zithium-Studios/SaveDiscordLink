package com.jaoow.discordbridge.database.querys;

import com.jaoow.discordbridge.DiscordLink;
import lombok.Builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Builder
public class Query {

    private final Connection connection;
    private final QueryType query;
    @Builder.Default private final Object[] parameters = new Object[]{};

    public PreparedStatement generate() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query.getQuery());
        try {
            if (parameters.length > 0) {
                for (int i = 1; i <= parameters.length; i++) {
                    Object parameter = parameters[i - 1];
                    if (parameter == null) {
                        throw new IllegalArgumentException("The parameter can't be null.");
                    }
                    if (parameter instanceof String) {
                        statement.setString(i, parameter.toString());
                    } else if (parameter instanceof UUID) {
                        statement.setString(i, parameter.toString());
                    } else if (parameter instanceof Boolean) {
                        statement.setBoolean(i, (boolean) parameter);
                    } else if (parameter instanceof Integer) {
                        statement.setInt(i, (int) parameter);
                    } else if (parameter instanceof Long) {
                        statement.setLong(i, (long) parameter);
                    } else if (parameter instanceof Double) {
                        statement.setDouble(i, (double) parameter);
                    } else {
                        DiscordLink.getPluginLogger().warning("We can't put the object '" + parameter.toString() + "' in the query.");
                    }
                }
            }
        } catch (SQLException ex) {
            DiscordLink.getPluginLogger().warning("An internal error has occurred while trying to execute a query in the database, check the logs to get more information.");
        }
        return statement;
    }

}
