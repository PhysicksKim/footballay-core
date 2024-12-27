package com.gyechunsik.scoreboard.domain.football.preference.persistence;

import com.gyechunsik.scoreboard.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_file_paths")
public class UserFilePath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String domain;

    @Builder.Default
    @Column(nullable = false)
    private String prefixPath = "";

    @Builder.Default
    @Column(nullable = false)
    private String suffixPath = ""; // 기본값 설정

    @Column(nullable = false, unique = true)
    private String userPathHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPathCategory userPathCategory;

    /**
     * JPA 엔티티가 저장되기 전에 domain 의 뒤 슬래시와 prefixPath, suffixPath의 앞뒤 슬래시를 제거합니다.
     */
    @PrePersist
    @PreUpdate
    private void cleanPaths() {
        if (domain != null) {
            domain = removeTrailingSlash(domain);
        }
        if (prefixPath != null) {
            prefixPath = removeLeadingAndTrailingSlash(prefixPath);
        }
        if (suffixPath != null) {
            suffixPath = removeLeadingAndTrailingSlash(suffixPath);
        }
    }

    /**
     * 전체 파일 경로를 생성합니다.
     * 형식: {domain}/{prefixPath}/{userPathHash}/{suffixPath}/
     * prefixPath 또는 suffixPath가 비어있을 경우, 중복된 슬래시를 방지합니다.
     *
     * @return 완성된 전체 파일 경로
     */
    public String getFullPath() {
        StringBuilder fullPath = new StringBuilder();

        // Append domain
        fullPath.append(domain);

        // Append '/' if prefixPath is not empty
        if (!prefixPath.isEmpty()) {
            fullPath.append("/");
            fullPath.append(prefixPath);
        }

        // Append '/' and userPathHash
        fullPath.append("/");
        fullPath.append(userPathHash);

        // Append '/' and suffixPath if not empty
        if (!suffixPath.isEmpty()) {
            fullPath.append("/");
            fullPath.append(suffixPath);
        }

        // Ensure the path ends with '/'
        if (!fullPath.toString().endsWith("/")) {
            fullPath.append("/");
        }

        return fullPath.toString();
    }

    /**
     * 문자열의 앞뒤 슬래시를 제거합니다.
     *
     * @param path 처리할 문자열
     * @return 앞뒤 슬래시가 제거된 문자열
     */
    private String removeLeadingAndTrailingSlash(String path) {
        if (path == null) {
            return null;
        }
        return removeLeadingSlash(removeTrailingSlash(path));
    }

    /**
     * 문자열의 앞 슬래시를 제거합니다.
     *
     * @param path 처리할 문자열
     * @return 앞 슬래시가 제거된 문자열
     */
    private String removeLeadingSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    /**
     * 문자열의 뒤 슬래시를 제거합니다.
     *
     * @param path 처리할 문자열
     * @return 뒤 슬래시가 제거된 문자열
     */
    private String removeTrailingSlash(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
