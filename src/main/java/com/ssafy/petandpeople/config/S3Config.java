package com.ssafy.petandpeople.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    private static final Dotenv DOTENV = Dotenv.load();

    private static final String ACCESS_KEY = DOTENV.get("AWS_ACCESS_KEY");
    private static final String SECRET_KEY = DOTENV.get("AWS_SECRET_KEY");
    private static final String BUCKET_NAME = DOTENV.get("AWS_BUCKET_NAME");
    private static final String REGION = DOTENV.get("AWS_REGION_STATIC");

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentials basicAWSCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(REGION).build();
    }

    public static String getBucketName() {
        return BUCKET_NAME;
    }

}