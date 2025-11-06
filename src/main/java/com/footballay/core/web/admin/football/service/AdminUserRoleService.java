package com.footballay.core.web.admin.football.service;

import com.footballay.core.domain.user.UserInfoDto;
import com.footballay.core.domain.user.UserRoot;
import com.footballay.core.web.admin.football.response.UserInfoResponse;
import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.common.service.ApiCommonResponseService;
import jakarta.annotation.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AdminUserRoleService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminUserRoleService.class);
    private final ApiCommonResponseService apiCommonResponseService;
    private final UserRoot userRoot;

    /**
     * 세션 회원의 정보를 가져온다.
     * @param auth
     * @param requestUrl
     * @return
     */
    public ApiResponse<UserInfoResponse> getUserInfo(@Nullable Authentication auth, String requestUrl) {
        try {
            if (auth == null) {
                log.info("Authentication is null while getting user info in AdminUserRoleService");
                return apiCommonResponseService.createFailureResponse("anonymous user", requestUrl);
            }
            UserInfoDto userInfo = userRoot.getUserInfo(auth.getName());
            UserInfoResponse[] responseArr = {toUserInfoResponse(userInfo.nickname(), userInfo.roles(), userInfo.profileImage(), userInfo.preferenceKey())};
            return apiCommonResponseService.createSuccessResponse(responseArr, requestUrl);
        } catch (Exception e) {
            log.error("getUserInfo error: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("user info error", requestUrl);
        }
    }

    private UserInfoResponse toUserInfoResponse(String nickname, String[] roles, String profileImage, String preferenceKey) {
        return new UserInfoResponse(nickname, roles, profileImage, preferenceKey);
    }

    public AdminUserRoleService(final ApiCommonResponseService apiCommonResponseService, final UserRoot userRoot) {
        this.apiCommonResponseService = apiCommonResponseService;
        this.userRoot = userRoot;
    }
}
