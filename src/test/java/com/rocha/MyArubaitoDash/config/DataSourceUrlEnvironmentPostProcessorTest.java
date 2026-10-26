package com.rocha.MyArubaitoDash.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataSourceUrlEnvironmentPostProcessorTest {

    private final DataSourceUrlEnvironmentPostProcessor processor = new DataSourceUrlEnvironmentPostProcessor();

    @Test
    void leavesJdbcUrlUntouched() {
        StandardEnvironment environment = environmentWith(Map.of(
                "spring.datasource.url", "jdbc:mysql://db.internal:3306/railway?useSSL=true",
                "spring.datasource.username", "appuser"
        ));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("jdbc:mysql://db.internal:3306/railway?useSSL=true", environment.getProperty("spring.datasource.url"));
        assertEquals("appuser", environment.getProperty("spring.datasource.username"));
        assertNull(environment.getPropertySources().get("mysqlUrlRewrite"));
    }

    @Test
    void rewritesMysqlUrlAndReadsCredentialsFromIt() {
        StandardEnvironment environment = environmentWith(Map.of(
                "spring.datasource.url", "mysql://railuser:p%40ss@mysql.railway.internal:3306/railway"
        ));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals(
                "jdbc:mysql://mysql.railway.internal:3306/railway?" + DataSourceUrlEnvironmentPostProcessor.DEFAULT_QUERY,
                environment.getProperty("spring.datasource.url"));
        assertEquals("railuser", environment.getProperty("spring.datasource.username"));
        assertEquals("p@ss", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void explicitUsernameAndPasswordWinOverTheUrl() {
        StandardEnvironment environment = environmentWith(Map.of(
                "spring.datasource.url", "mysql://urluser:urlpass@db:3306/myarubaito",
                "DATABASE_USER", "keptuser",
                "DATABASE_PASSWORD", "keptpass",
                "spring.datasource.username", "keptuser",
                "spring.datasource.password", "keptpass"
        ));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals(
                "jdbc:mysql://db:3306/myarubaito?" + DataSourceUrlEnvironmentPostProcessor.DEFAULT_QUERY,
                environment.getProperty("spring.datasource.url"));
        assertEquals("keptuser", environment.getProperty("spring.datasource.username"));
        assertEquals("keptpass", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void keepsQueryStringFromTheMysqlUrl() {
        StandardEnvironment environment = environmentWith(Map.of(
                "spring.datasource.url", "mysql://db.internal:3306/myarubaito?useSSL=true&serverTimezone=America/Toronto"
        ));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals(
                "jdbc:mysql://db.internal:3306/myarubaito?useSSL=true&serverTimezone=America/Toronto",
                environment.getProperty("spring.datasource.url"));
    }

    private static StandardEnvironment environmentWith(Map<String, String> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", new HashMap<>(values)));
        return environment;
    }
}
