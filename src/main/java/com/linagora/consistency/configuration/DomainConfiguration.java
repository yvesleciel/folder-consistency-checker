package com.linagora.consistency.configuration;

import com.linagora.consistency.domain.port.driven.ForRetrievingGlobalFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUserFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUsers;
import com.linagora.consistency.domain.port.driving.ForDetectingInconsistencies;
import com.linagora.consistency.domain.service.InconsistencyDetectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spring configuration for domain layer.
 * Wires domain services with their dependencies.
 */
@Configuration
public class DomainConfiguration {

    @Bean
    public ForDetectingInconsistencies inconsistencyDetector(
        ForRetrievingUsers userRetriever,
        ForRetrievingUserFolders userFoldersRetriever,
        ForRetrievingGlobalFolders globalFoldersRetriever,
        ExecutorService executorService
    ) {
        return new InconsistencyDetectionService(
            userRetriever,
            userFoldersRetriever,
            globalFoldersRetriever,
            executorService
        );
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        // Fixed thread pool for parallel API calls
        // Size based on typical number of users (can be configured)
        int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
        return Executors.newFixedThreadPool(threadPoolSize);
    }
}
