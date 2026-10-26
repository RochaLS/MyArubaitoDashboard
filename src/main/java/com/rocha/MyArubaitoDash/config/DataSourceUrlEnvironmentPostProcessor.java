package com.rocha.MyArubaitoDash.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accepts a {@code mysql://user:pass@host:port/db} URL (Railway's {@code MYSQL_URL},
 * Coolify's internal URL) and rewrites it to a JDBC URL. A URL that is already
 * {@code jdbc:mysql://} is left untouched, so an existing Railway {@code DATABASE_URL}
 * keeps working.
 */
public class DataSourceUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String DEFAULT_QUERY = "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    private static final Pattern MYSQL_URL = Pattern.compile(
            "^mysql://(?:([^:@/?#]+)(?::([^@/?#]*))?@)?([^:/?#]+)(?::(\\d+))?/([^?#]+)/?(?:\\?(.*))?$");

    @Override
    public int getOrder() {
        // Run after application.properties and environment variables are loaded.
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url");
        if (url == null || !url.startsWith("mysql://")) {
            return;
        }

        Matcher matcher = MYSQL_URL.matcher(url.trim());
        if (!matcher.matches()) {
            System.err.println("Datasource URL started with mysql:// but could not be parsed. It was left unchanged and no credentials were logged.");
            return;
        }

        String user = decode(matcher.group(1));
        String password = decode(matcher.group(2));
        String host = matcher.group(3);
        String port = matcher.group(4) == null ? "3306" : matcher.group(4);
        String database = matcher.group(5);
        String query = matcher.group(6);

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?" + (query == null || query.isBlank() ? DEFAULT_QUERY : query);

        Map<String, Object> overrides = new HashMap<>();
        overrides.put("spring.datasource.url", jdbcUrl);
        if (user != null && !isExplicitlySet(environment, "DATABASE_USER", "MYSQLUSER", "MYSQL_USER", "SPRING_DATASOURCE_USERNAME")) {
            overrides.put("spring.datasource.username", user);
        }
        if (password != null && !isExplicitlySet(environment, "DATABASE_PASSWORD", "MYSQLPASSWORD", "MYSQL_PASSWORD", "SPRING_DATASOURCE_PASSWORD")) {
            overrides.put("spring.datasource.password", password);
        }

        environment.getPropertySources().addFirst(new MapPropertySource("mysqlUrlRewrite", overrides));
        System.err.println("Datasource URL used the mysql:// form and was rewritten to jdbc:mysql://. Credentials were not logged.");
    }

    private static boolean isExplicitlySet(ConfigurableEnvironment environment, String... keys) {
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (value != null && !value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static String decode(String value) {
        if (value == null) {
            return null;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
