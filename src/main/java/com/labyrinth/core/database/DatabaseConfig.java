package com.labyrinth.core.database;

import com.labyrinth.core.configs.ConfigBase;

public class DatabaseConfig extends ConfigBase {

    public DatabaseConfig() {
        super("database");
    }

    public String getHost() {
        return getString("database.host", "localhost");
    }

    public int getPort() {
        return getInt("database.port", 5433);
    }

    public String getDatabase() {
        return getString("database.name", "labyrinth");
    }

    public String getUsername() {
        return getString("database.username", "postgres");
    }

    public String getPassword() {
        return getString("database.password", "root");
    }

    public int getMaxPoolSize() {
        return getInt("pool.maxSize", 10);
    }

    public int getMinIdle() {
        return getInt("pool.minIdle", 2);
    }

    public long getIdleTimeout() {
        return getInt("pool.idleTimeout", 600000);
    }

    public long getConnectionTimeout() {
        return getInt("pool.connectionTimeout", 30000);
    }

    public long getMaxLifetime() {
        return getInt("pool.maxLifetime", 1800000);
    }

    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", getHost(), getPort(), getDatabase());
    }
}