package com.beta.docker;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@ActiveProfiles("test")
public class SearchSyncTestContainer {

    protected static final Network network = Network.newNetwork();

    protected static MySQLContainer<?> mysql;
    protected static ElasticsearchContainer elasticsearch;
    protected static GenericContainer<?> logstash;

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

        logstash = new GenericContainer<>(DockerImageName.parse("docker.elastic.co/logstash/logstash:8.18.8"))
                .withNetwork(network)
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("logstash/pipelines.yml"),
                        "/usr/share/logstash/config/pipelines.yml"
                )
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("logstash/posts.conf"),
                        "/usr/share/logstash/pipeline/posts.conf"
                )
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("logstash/users.conf"),
                        "/usr/share/logstash/pipeline/users.conf"
                )
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("logstash/hashtags.conf"),
                        "/usr/share/logstash/pipeline/hashtags.conf"
                )
                .withCommand(
                        "bash",
                        "-lc",
                        "curl -fsSL -o /tmp/mysql-connector-j-8.3.0.jar " +
                                "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar " +
                                "&& exec /usr/share/logstash/bin/logstash"
                )
                .waitingFor(
                        Wait.forLogMessage(".*Successfully started Logstash API endpoint.*", 1)
                                .withStartupTimeout(Duration.ofMinutes(4))
                );

        logstash.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }
}
