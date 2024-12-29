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

    /**
     * 파일 경로의 도메인
     * <pre>
     *     ex) https://example.com
     * </pre>
     * <p>
     * 도메인 마지막에 슬래쉬를 제거하고 저장합니다.
     * ex) https://example.com/ -> https://example.com
     * </p>
     */
    @Column(nullable = false)
    private String domain;

    /**
     * 파일 경로의 접두사
     * <pre>
     * ex) {domain}/{prefixPath}/{userPathHash}/{suffixPath}/
     * </pre>
     * <p>
     *     prefixPath의 앞 뒤 슬래시를 제거하고 저장합니다.
     *     ex) /prefixPath/ -> prefixPath
     * </p>
     * <p>
     *     prefixPath 는 여러 중간 슬래쉬를 포함할 수 있습니다.
     *     ex) prefix/path/include/slashes
     * </p>
     *
     */
    @Builder.Default
    @Column(nullable = false)
    private String prefixPath = "";

    /**
     * 파일 경로의 접미사
     * <pre>
     *     ex) {domain}/{prefixPath}/{userPathHash}/{suffixPath}/
     * </pre>
     * <p>
     *     suffixPath의 앞 뒤 슬래시를 제거하고 저장합니다.
     *     ex) /suffixPath/ -> suffixPath
     * </p>
     * <p>
     *     suffixPath 는 여러 중간 슬래쉬를 포함할 수 있습니다.
     *     ex) suffix/path/include/slashes
     * </p>
     *
     */
    @Builder.Default
    @Column(nullable = false)
    private String suffixPath = "";

    /**
     * 사용자 구분을 위한 해시값. <br>
     * {유저, 카테고리} 조합으로 하나의 고유한 해시를 사용합니다. <br>
     * 같은 유저더라도 다른 카테고리를 사용할 경우 다른 해시값을 사용합니다. <br>
     * <pre>
     * 예시1. 아래의 case들은 모두 다른 userPathHash를 만듭니다.
     * case1) user1 + category_themecolor
     * case2) user1 + category_customphoto
     * case3) user2 + category_themecolor
     * </pre>
     * <pre>
     * 예시2. 아래의 case들은 같은 userPathHash를 사용해야 합니다.
     * case1) user1 + category_customphoto (file1.png)
     * case2) user1 + category_customphoto (file2.png)
     * </pre>
     */
    @Column(nullable = false, unique = true)
    private String userPathHash;

    /**
     * 사용자 경로의 카테고리 <br>
     * 같은 사용자일지라도 카테고리가 다르면 path 가 달라지므로 구분하고자 사용합니다. <br>
     * 카테고리가 다르면 prefixPath, suffixPath, userPathHash 가 다르게 저장됩니다. <br>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPathCategory userPathCategory;

    /**
     * JPA 엔티티가 저장되기 전에 domain 의 뒤 슬래시와 prefixPath, suffixPath 의 앞뒤 슬래시를 제거합니다.
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
     * <pre>
     * 형식: {domain}/{prefixPath}/{userPathHash}/{suffixPath}/
     * ex) https://example.com/prefixPath/userPathHash/suffixPath/
     * </pre>
     * prefixPath 또는 suffixPath가 비어있을 경우, 중복된 슬래시를 방지합니다.
     *
     * @return 완성된 전체 파일 경로
     */
    public String getFullPath() {
        StringBuilder fullPath = new StringBuilder();

        // Append domain
        fullPath.append(domain);

        // getPathWithoutDomain
        fullPath.append(getPathWithoutDomain());
        return fullPath.toString();
    }

    /**
     * 도메인을 제외하고 파일 경로 전까지 path 를 생성합니다. <br>
     * prefix 와 suffix 가 존재하지 않는 경우를 고려하여 path 를 아래 예시와 같이 생성합니다.
     * <pre>
     *     형식: /{prefixPath}/{userPathHash}/{suffixPath}/
     *     ex) /prefixPath/userPathHash/suffixPath/
     *     ex) /prefixPath/userPathHash/
     *     ex) /userPathHash/suffixPath/
     *     ex) /userPathHash/
     * </pre>
     * @return 도메인을 제외한 파일 경로 전까지의 path
     */
    public String getPathWithoutDomain() {
        StringBuilder pathWithoutDomain = new StringBuilder();

        // Append '/' if prefixPath is not empty
        if (!prefixPath.isEmpty()) {
            pathWithoutDomain.append("/");
            pathWithoutDomain.append(prefixPath);
        }

        // Append '/' and userPathHash
        pathWithoutDomain.append("/");
        pathWithoutDomain.append(userPathHash);

        // Append '/' and suffixPath if not empty
        if (!suffixPath.isEmpty()) {
            pathWithoutDomain.append("/");
            pathWithoutDomain.append(suffixPath);
        }

        // Ensure the path ends with '/'
        if (!pathWithoutDomain.toString().endsWith("/")) {
            pathWithoutDomain.append("/");
        }

        return pathWithoutDomain.toString();
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
