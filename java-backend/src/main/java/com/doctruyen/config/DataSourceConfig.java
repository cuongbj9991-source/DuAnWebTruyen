package com.doctruyen.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.TimeZone;

@Configuration
public class DataSourceConfig {

    public DataSourceConfig() {
        // Set JVM timezone to UTC to prevent PostgreSQL rejection
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        // Don't specify timezone - let server handle it
        config.setJdbcUrl("jdbc:postgresql://gondola.proxy.rlwy.net:31141/railway?sslmode=allow&ApplicationName=story-service");
        config.setUsername("postgres");
        config.setPassword("vpWAUpPfLcXMXaLeraiFAeaISCtWwXHl");
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setAutoCommit(true);
        
        return new HikariDataSource(config);
    }
}
