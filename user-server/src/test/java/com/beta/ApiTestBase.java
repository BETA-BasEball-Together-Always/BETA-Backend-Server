package com.beta;

import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.docker.MysqlRedisTestContainer;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class ApiTestBase extends MysqlRedisTestContainer {

    @MockitoBean
    protected AbstractAuthenticationDetailsProvider authProvider;

    @MockitoBean
    protected ObjectStorage objectStorage;

    @MockitoBean
    protected OracleCloudStorageClient oracleCloudStorageClient;
}
