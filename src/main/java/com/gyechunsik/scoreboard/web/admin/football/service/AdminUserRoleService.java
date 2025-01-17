package com.gyechunsik.scoreboard.web.admin.football.service;

import com.gyechunsik.scoreboard.domain.user.UserInfoDto;
import com.gyechunsik.scoreboard.domain.user.UserRoot;
import com.gyechunsik.scoreboard.web.admin.football.response.UserInfoResponse;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminUserRoleService {

    private final ApiCommonResponseService apiCommonResponseService;
    private final UserRoot userRoot;

    public ApiResponse<UserInfoResponse> getUserInfo(@Nullable Authentication auth, String requestUrl) {
        try{
            if(auth == null) {
                log.info("Authentication is null while getting user info in AdminUserRoleService");
                return apiCommonResponseService.createFailureResponse("anonymous user", requestUrl);
            }
            UserInfoDto userInfo = userRoot.getUserInfo(auth.getName());
            UserInfoResponse[] responseArr = {toUserInfoResponse(userInfo.nickname(), userInfo.roles(), userInfo.profileImage())};
            return apiCommonResponseService.createSuccessResponse(responseArr, requestUrl);
        } catch (Exception e) {
            log.error("getUserInfo error: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("user info error", requestUrl);
        }
    }

    private UserInfoResponse toUserInfoResponse(String nickname, String[] roles, String profileImage) {
        return new UserInfoResponse(nickname, roles, profileImage);
    }

}
