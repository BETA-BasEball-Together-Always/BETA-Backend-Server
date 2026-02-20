package com.beta.docker;

import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@ActiveProfiles("test")
public class MysqlEsLogstashTestContainer extends MysqlEsTestContainer {

    protected static GenericContainer<?> logstash;

    static {
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
}
