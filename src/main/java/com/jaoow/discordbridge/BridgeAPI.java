package com.jaoow.discordbridge;

import com.jaoow.discordbridge.database.Database;
import com.jaoow.discordbridge.database.querys.Query;
import com.jaoow.discordbridge.database.querys.QueryType;
import com.jaoow.discordbridge.database.querys.Response;
import com.jaoow.discordbridge.model.RewardType;
import com.jaoow.discordbridge.model.UserData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.jaoow.discordbridge.config.Settings.*;

@AllArgsConstructor
public class BridgeAPI {

    @Getter
    private final Database database;

    public void createTable() {
        CompletableFuture.runAsync(() -> {
            val createTable = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.CREATE_TABLE)
                    .build();

            database.executeUpdate(createTable);
        });
    }

    public CompletableFuture<Boolean> hasRegister(String discordId) {
        return CompletableFuture.supplyAsync(() -> {
            Query playerQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.SELECT_FROM_DISCORD)
                    .parameters(new Object[]{discordId})
                    .build();

            try (ResultSet result = database.executeQuery(playerQuery)) {
                return result.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public CompletableFuture<Boolean> isAssigned(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            Query playerQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.SELECT_FROM_UUID)
                    .parameters(new Object[]{player.getUniqueId()})
                    .build();

            try (ResultSet result = database.executeQuery(playerQuery)) {
                return result.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public CompletableFuture<Void> consumeAll(Consumer<ResultSet> resultSetConsumer) {
        return CompletableFuture.runAsync(() -> {
            Query playerQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.SELECT)
                    .build();

            try (ResultSet result = database.executeQuery(playerQuery)) {
                while (result.next()) {
                    resultSetConsumer.accept(result);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }



    public CompletableFuture<Response> assignPlayer(OfflinePlayer player, String codeStr) {
        return CompletableFuture.supplyAsync(() -> {
            // null check
            if (codeStr.equals("null")) {
                return Response.NOT_FOUND;
            }

            Query playerQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.SELECT_FROM_CODE)
                    .parameters(new Object[]{codeStr})
                    .build();

            try (ResultSet result = database.executeQuery(playerQuery)) {
                if (result.next()) {
                    Query updateQuery = Query.builder()
                            .connection(database.getConnection())
                            .query(QueryType.ASSIGN_PLAYER)
                            .parameters(new Object[]{player.getUniqueId(), "null", codeStr}).build();

                    database.executeUpdate(updateQuery);
                    return Response.SUCCESS;
                } else {
                    return Response.NOT_FOUND;
                }
            } catch (SQLException e) {
                return Response.ERROR;
            }
        });
    }

    public CompletableFuture<Response> injectData(Object[] array) {
        return CompletableFuture.supplyAsync(() -> {
            val injectQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.INSERT_PLAYER)
                    .parameters(array)
                    .build();

            return database.executeUpdate(injectQuery) ? Response.SUCCESS : Response.ERROR;
        });
    }

    public CompletableFuture<Response> injectMember(String discordId, String code) {
        return CompletableFuture.supplyAsync(() -> {
            val injectQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.INSERT_PLAYER)
                    .parameters(new Object[]{
                            discordId, "", code, 0, 0, 0
                    })
                    .build();

            return database.executeUpdate(injectQuery) ? Response.SUCCESS : Response.ERROR;
        });
    }

    public CompletableFuture<Response> injectPlayer(String discordId, String uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            val injectQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.INSERT_PLAYER)
                    .parameters(new Object[]{
                            discordId, uniqueId, "null", false, false, false
                    })
                    .build();

            return database.executeUpdate(injectQuery) ? Response.SUCCESS : Response.ERROR;
        });
    }

    public CompletableFuture<Response> deletePlayer(String uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            final Query deleteQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.DELETE_FROM_UUID)
                    .parameters(new Object[]{uniqueId})
                    .build();

            return database.executeUpdate(deleteQuery) ? Response.SUCCESS : Response.NOT_FOUND;
        });
    }

    public CompletableFuture<Optional<UserData>> getUserData(UUID key) {
        return CompletableFuture.supplyAsync(() -> {
            Query playerQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.SELECT_FROM_UUID)
                    .parameters(new Object[]{key})
                    .build();

            try (ResultSet result = database.executeQuery(playerQuery)) {
                if (result.next()) {
                    String discordId = result.getString(DISCORD_COLLUM);
                    UserData userData = new UserData(
                            new HashMap<RewardType, Long>() {{
                                put(RewardType.LINK, result.getLong(LINK_REWARD_COLLUM));
                                put(RewardType.BOOSTER, result.getLong(BOOSTER_REWARD_COLUMN));
                            }}
                    );
                    checkBooster(discordId, userData);
                    return Optional.of(userData);
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<Response> updateUserData(OfflinePlayer player, UserData userData) {
        return CompletableFuture.supplyAsync(() -> {
            val updateQuery = Query.builder()
                    .connection(database.getConnection())
                    .query(QueryType.CLAIM_REWARD)
                    .parameters(new Object[]{
                            userData.getLastOpenFor(RewardType.LINK),
                            userData.getLastOpenFor(RewardType.BOOSTER),
                            player.getUniqueId()}).build();

            database.executeUpdate(updateQuery);
            return Response.SUCCESS;
        });
    }

    private void checkBooster(String id, UserData userData) {
        Guild guild = DiscordLink.getInstance().getJda().getGuilds().get(0);
        Member member = guild.retrieveMemberById(id).complete();
        userData.getBoosterStatus().set(member.getTimeBoosted() != null);
    }
}
