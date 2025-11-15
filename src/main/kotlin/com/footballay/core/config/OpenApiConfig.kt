package com.footballay.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * SpringDoc OpenAPI 설정
 *
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * API Docs JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Footballay Backend API")
                    .version("1.0.0")
                    .description(
                        """
                        라이브 축구 경기 스탯 앱 풋볼레이 Footballay 입니다. 

                        **Admin Access:**
                        관리자 로그인을 위해서는 먼저 /login 엔드포인트를 통해 인증을 받아야 합니다.  
                        자세한 로그인 방법은 Admin Page Repository 코드를 참고하세요.  
                        """.trimIndent(),
                    ).contact(
                        Contact()
                            .name("Footballay Team"),
                    ),
            ).servers(
                listOf(
                    Server().url("https://localhost:8083").description("Development"),
                    Server().url("https://footballay.com").description("Production"),
                ),
            ).components(
                Components()
                    .addSecuritySchemes(
                        "cookieAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.COOKIE)
                            .name("JSESSIONID")
                            .description("Session cookie authentication. Login at /login first, then the session cookie will be used automatically."),
                    ),
            )

    /**
     * Admin API 그룹
     * - Fixture management (available status, job registration)
     * - ApiSports sync operations
     * - League/Team/Player management
     */
    @Bean
    fun adminApiGroup(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("admin")
            .displayName("Admin API")
            .pathsToMatch("/api/v1/admin/**")
            .build()

    /**
     * Legacy Admin API 그룹 (v0)
     * - 기존 Admin API (v1 이전 버전)
     */
    @Bean
    fun legacyAdminGroup(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("legacy-v0-admin")
            .displayName("Legacy Admin API")
            .pathsToMatch("/api/admin/**")
            .build()

    /**
     * Public API 그룹
     * - Football stream data
     * - Available fixtures
     * - Live match status
     */
    @Bean
    fun publicApiGroup(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("public")
            .displayName("Public API")
            .pathsToMatch("/api/football/**")
            .build()
}
