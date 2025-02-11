package com.zj.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceConfiguration {
    public static final String MARIADB_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String JDBC_FORMAT_URL = "jdbc:mysql://%s/%s?serverTimezone=UTC&allowPublicKeyRetrieval" +
            "=true&useSSL=false&useUnicode=true&characterEncoding=utf-8";
    public static final String WINDY = "windy";
    @Value("${DB_HOST}")
    private String dbHost;

    @Value("${DB_USERNAME:windy}")
    private String dbUsername;

    @Value("${DB_PASSWORD:windy!123}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        String jdbcUrl = String.format(JDBC_FORMAT_URL, dbHost, WINDY);
        log.info("connect mysql url={}", jdbcUrl);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(MARIADB_JDBC_DRIVER);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }
}
