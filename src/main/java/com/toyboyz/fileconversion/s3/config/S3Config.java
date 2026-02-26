package com.toyboyz.fileconversion.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    //#1.추후 main 머지할 때 제거
    private final String profileName = "toyboyz-sso";

    @Bean
    public S3Client s3Client(@Value("${app.s3.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                //#2.추후 Default로 변경해야함
                .credentialsProvider(ProfileCredentialsProvider.create(profileName))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${app.s3.region}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                // toyboyz-sso 프로필의 인증 정보를 사용하도록 설정
                .credentialsProvider(ProfileCredentialsProvider.create(profileName))
                .build();
    }

//    @Bean
//    public S3Client s3Client(@Value("${app.s3.region}") String region) {
//        return S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
//    }
//
//    @Bean
//    public S3Presigner s3Presigner(@Value("${app.s3.region}") String region) {
//        return S3Presigner.builder()
//                .region(Region.of(region))
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
//    }
}
