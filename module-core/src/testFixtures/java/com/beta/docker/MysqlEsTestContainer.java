package com.beta.docker;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
public class MysqlEsTestContainer {

    protected static final Network network = Network.newNetwork();

    protected static MySQLContainer<?> mysql;
    protected static ElasticsearchContainer elasticsearch;

    static {
        mysql = new MySQLContainer<>("mysql:8.4.5")
                .withDatabaseName("testdb")
                .withUsername("root")
                .withPassword("test")
                .withNetwork(network)
                .withNetworkAliases("mysql");

        elasticsearch = new ElasticsearchContainer(
                DockerImageName.parse("dhgudtmxhs/es-nori:8.18.8")
                        .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                .withNetwork(network)
                .withNetworkAliases("elasticsearch");

        mysql.start();
        elasticsearch.start();
    }

    @DynamicPropertySource
    static void configure_properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.elasticsearch.uris", () -> "http://" + elasticsearch.getHttpHostAddress());
    }
}
