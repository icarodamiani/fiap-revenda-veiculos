package io.fiap.revenda.veiculos.driven.configuration;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfiguration {

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Value("${aws.region:}")
    private String region;

    @Value("${aws.sqs.uri:}")
    private String uri;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        if (StringUtils.hasText(uri)) {
            return SqsAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .httpClient(NettyNioAsyncHttpClient.builder().build())
                .endpointOverride(URI.create(uri))
                .region(Region.of(region))
                .build();
        }

        return SqsAsyncClient.builder()
            .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
            .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
            .build();
    }
}
