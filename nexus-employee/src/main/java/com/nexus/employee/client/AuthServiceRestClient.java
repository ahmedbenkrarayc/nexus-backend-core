package com.nexus.employee.client;

import com.nexus.employee.client.dto.AuthCurrentUserResponse;
import com.nexus.employee.client.dto.AuthRegisterRequest;
import com.nexus.employee.client.dto.AuthRegisterResponse;
import com.nexus.employee.client.dto.AuthUserRolesResponse;
import com.nexus.employee.dto.request.user.UserCreateRequest;
import com.nexus.employee.exception.ExternalServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthServiceRestClient implements AuthServiceClient {

    private final RestClient authServiceRestClient;

    public AuthServiceRestClient(@Qualifier("authServiceHttpClient") RestClient authServiceRestClient) {
        this.authServiceRestClient = authServiceRestClient;
    }

    @Override
    public Long registerUser(UserCreateRequest request) {
        try {
            AuthRegisterResponse response = authServiceRestClient.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> forwardCallerHeaders(headers))
                    .body(new AuthRegisterRequest(
                            request.username(),
                            request.email(),
                            request.password(),
                            request.roles()
                    ))
                    .retrieve()
                    .body(AuthRegisterResponse.class);

            if (response == null || response.userId() == null) {
                throw new ExternalServiceException("Auth service returned an empty register response");
            }

            return response.userId();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(buildError("register user", exception), exception);
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException(
                    "Unable to register user in auth service: "
                            + exception.getClass().getSimpleName()
                            + " - "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    @Override
    public void deactivateUser(Long userId) {
        try {
            authServiceRestClient.patch()
                    .uri("/api/auth/users/{userId}/deactivate", userId)
                    .headers(this::forwardCallerHeaders)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(buildError("deactivate user", exception), exception);
        } catch (Exception exception) {
            throw new ExternalServiceException("Unable to deactivate user in auth service", exception);
        }
    }

    @Override
    public boolean userHasRole(Long userId, String roleName) {
        try {
            AuthUserRolesResponse response = authServiceRestClient.get()
                    .uri("/api/auth/users/{userId}/roles", userId)
                    .headers(this::forwardCallerHeaders)
                    .retrieve()
                    .body(AuthUserRolesResponse.class);

            if (response == null || response.roles() == null) {
                throw new ExternalServiceException("Auth service returned an empty user roles response");
            }

            return response.roles().stream().anyMatch(role -> roleName.equalsIgnoreCase(role));
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(buildError("get user roles", exception), exception);
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException("Unable to read user roles from auth service", exception);
        }
    }

    @Override
    public java.util.List<Long> getUserIdsByRole(String roleName) {
        try {
            Long[] userIds = authServiceRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/auth/users/by-role")
                            .queryParam("role", roleName)
                            .build())
                    .headers(this::forwardCallerHeaders)
                    .retrieve()
                    .body(Long[].class);
            return userIds == null ? java.util.List.of() : java.util.Arrays.asList(userIds);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound exception) {
            return java.util.List.of();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(buildError("get users by role", exception), exception);
        } catch (Exception exception) {
            throw new ExternalServiceException("Unable to read users by role from auth service", exception);
        }
    }

    @Override
    public AuthCurrentUserResponse getCurrentUser() {
        try {
            AuthCurrentUserResponse response = authServiceRestClient.get()
                    .uri("/api/auth/me")
                    .headers(this::forwardCallerHeaders)
                    .retrieve()
                    .body(AuthCurrentUserResponse.class);

            if (response == null || response.userId() == null || response.roles() == null) {
                throw new ExternalServiceException("Auth service returned an empty current user response");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException(buildError("get current user", exception), exception);
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalServiceException("Unable to read current user from auth service", exception);
        }
    }

    private void forwardCallerHeaders(HttpHeaders headers) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        String cookie = request.getHeader(HttpHeaders.COOKIE);
        if (cookie != null && !cookie.isBlank()) {
            headers.set(HttpHeaders.COOKIE, cookie);
        }
    }

    private String buildError(String action, RestClientResponseException exception) {
        String body = exception.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return "Auth service failed to " + action + " (status " + exception.getStatusCode() + ")";
        }
        return "Auth service failed to " + action + " (status " + exception.getStatusCode() + "): " + body;
    }
}