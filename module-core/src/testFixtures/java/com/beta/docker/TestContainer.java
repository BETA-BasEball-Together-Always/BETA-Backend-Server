package com.beta.docker;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
public class TestContainer {
    protected static MySQLContainer<?> mysql;
    protected static GenericContainer<?> redis;
    protected static ElasticsearchContainer elasticsearch;

    static {
        mysql = new MySQLContainer<>("mysql:8.4.5")
                .withDatabaseName("testdb")
                .withUsername("root")
                .withPassword("test");

        redis = new GenericContainer<>("redis:7.2")
                .withExposedPorts(6379)
                .withCommand("redis-server");

        elasticsearch = new ElasticsearchContainer(
                DockerImageName.parse("dhgudtmxhs/es-nori:8.11.0")
                        .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");

        mysql.start();
        redis.start();
        elasticsearch.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL TestContainer 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Redis TestContainer 설정
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // Elasticsearch TestContainer 설정
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }
}
