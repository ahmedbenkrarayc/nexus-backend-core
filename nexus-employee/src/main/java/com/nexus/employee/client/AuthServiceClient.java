package com.nexus.employee.client;

import com.nexus.employee.client.dto.AuthCurrentUserResponse;
import com.nexus.employee.dto.request.user.UserCreateRequest;

public interface AuthServiceClient {

    Long registerUser(UserCreateRequest request);

    void deactivateUser(Long userId);

    boolean userHasRole(Long userId, String roleName);

    java.util.List<Long> getUserIdsByRole(String roleName);

    AuthCurrentUserResponse getCurrentUser();
}