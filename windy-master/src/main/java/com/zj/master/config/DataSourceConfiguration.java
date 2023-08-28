package com.zj.master.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {
    public static final String MARIADB_JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    public static final String JDBC_FORMAT_URL = "jdbc:mariadb://%s/%s?serverTimezone=UTC&allowPublicKeyRetrieval=true";

    @Value("${DB_HOST}")
    private String dbHost;

    @Value("${DB_NAME}")
    private String dbName;

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        String jdbcUrl = String.format(JDBC_FORMAT_URL, dbHost, dbName);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(MARIADB_JDBC_DRIVER);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }
}
