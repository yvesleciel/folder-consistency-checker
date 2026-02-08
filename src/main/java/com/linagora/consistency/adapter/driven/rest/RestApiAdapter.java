package com.linagora.consistency.adapter.driven.rest;

import com.linagora.consistency.adapter.driven.rest.dto.GlobalFolderDto;
import com.linagora.consistency.adapter.driven.rest.dto.UserFolderDto;
import com.linagora.consistency.domain.model.*;
import com.linagora.consistency.domain.port.driven.ForRetrievingGlobalFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUserFolders;
import com.linagora.consistency.domain.port.driven.ForRetrievingUsers;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * REST adapter implementing all driven ports for external API communication.
 * Uses Spring WebClient for reactive HTTP calls.
 * This adapter translates between DTOs and domain models.
 */
public class RestApiAdapter implements
    ForRetrievingUsers,
    ForRetrievingUserFolders,
    ForRetrievingGlobalFolders {

    private final WebClient webClient;
    private final Duration timeout;

    public RestApiAdapter(WebClient webClient, Duration timeout) {
        this.webClient = Objects.requireNonNull(webClient, "WebClient cannot be null");
        this.timeout = Objects.requireNonNull(timeout, "Timeout cannot be null");
    }

    @Override
    public List<Email> retrieveAllUsers() {
        return webClient.get()
            .uri("/users")
            .retrieve()
            .bodyToMono(String[].class)
            .flatMapMany(Flux::fromArray)
            .map(Email::of)
            .collectList()
            .timeout(timeout)
            .block(); // Block here as domain service expects synchronous result
    }

    @Override
    public UserFolders retrieveFoldersForUser(Email userEmail) {
        List<UserFolder> folders = webClient.get()
            .uri("/users/{email}/folders", userEmail.getValue())
            .retrieve()
            .bodyToFlux(UserFolderDto.class)
            .map(this::toDomainUserFolder)
            .collectList()
            .timeout(timeout)
            .block(); // Block here as domain service expects synchronous result

        return UserFolders.of(userEmail, folders);
    }

    @Override
    public List<GlobalFolder> retrieveAllGlobalFolders() {
        return webClient.get()
            .uri("/folders")
            .retrieve()
            .bodyToFlux(GlobalFolderDto.class)
            .map(this::toDomainGlobalFolder)
            .collectList()
            .timeout(timeout)
            .block(); // Block here as domain service expects synchronous result
    }

    private UserFolder toDomainUserFolder(UserFolderDto dto) {
        return UserFolder.of(
            FolderId.of(dto.id()),
            FolderName.of(dto.name())
        );
    }

    private GlobalFolder toDomainGlobalFolder(GlobalFolderDto dto) {
        return GlobalFolder.of(
            FolderId.of(dto.id()),
            Email.of(dto.user()),
            FolderName.of(dto.name())
        );
    }
}
