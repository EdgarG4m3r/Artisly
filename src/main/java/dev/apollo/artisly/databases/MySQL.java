package dev.apollo.artisly.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL {

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSSL;
    private HikariDataSource hikari;

    public MySQL(String host, String port, String database, String username, String password, boolean useSSL) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
    }

    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("minimumIdle", "2");
        config.addDataSourceProperty("maximumPoolSize", "10");
        config.addDataSourceProperty("maxLifetime", "300000");
        config.addDataSourceProperty("connectionTimeout", "5000");
        config.addDataSourceProperty("idleTimeout", "600000");
        config.addDataSourceProperty("autoCommit", "true");
        config.addDataSourceProperty("keepAliveTime", "30000");
        config.addDataSourceProperty("socketTimeout", "30000");
        config.addDataSourceProperty("validationTimeout", "500");
        config.addDataSourceProperty("leakDetectionThreshold", "30000");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("useUnbufferedIO", "false");
        config.addDataSourceProperty("useReadAheadInput", "false");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("useSSL", useSSL);
        hikari = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    public void disconnect() {
        hikari.close();
    }

}
