package com.footballay.core.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 더이상 AWS s3 를 사용하지 않습니다.
 */
@Deprecated(since = "0.1.0-20251108")
@Configuration
public class AwsS3Config {

    @Value("${aws.s3.accessKey:DEPRECATED}")
    private String accessKey;

    @Value("${aws.s3.secretKey:DEPRECATED}")
    private String secretKey;

    @Value("${aws.s3.region:DEPRECATED}")
    private String region;

    @Bean
    public AmazonS3 amazonS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

}
