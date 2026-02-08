package com.linagora.consistency.configuration;

import com.linagora.consistency.adapter.driven.rest.RestApiAdapter;
import com.linagora.consistency.domain.port.driven.ForRetrievingGlobalFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUserFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUsers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Spring configuration for adapters.
 * Configures external dependencies (WebClient, REST adapters).
 */
@Configuration
public class AdapterConfiguration {

    @Value("${mock.api.base-url}")
    private String mockApiBaseUrl;

    @Value("${mock.api.timeout-seconds:10}")
    private int timeoutSeconds;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl(mockApiBaseUrl)
            .build();
    }

    @Bean
    public RestApiAdapter restApiAdapter(WebClient webClient) {
        return new RestApiAdapter(webClient, Duration.ofSeconds(timeoutSeconds));
    }

    @Bean
    public ForRetrievingUsers userRetriever(RestApiAdapter restApiAdapter) {
        return restApiAdapter;
    }

    @Bean
    public ForRetrievingUserFolders userFoldersRetriever(RestApiAdapter restApiAdapter) {
        return restApiAdapter;
    }

    @Bean
    public ForRetrievingGlobalFolders globalFoldersRetriever(RestApiAdapter restApiAdapter) {
        return restApiAdapter;
    }
}
