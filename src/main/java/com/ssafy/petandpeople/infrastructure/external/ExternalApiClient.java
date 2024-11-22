package com.ssafy.petandpeople.infrastructure.external;

import com.ssafy.petandpeople.application.dto.adoption.ApiRequestParams;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class ExternalApiClient {

    private final RestTemplate restTemplate;

    public ExternalApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String requestApi(ApiRequestParams params) {
        URI uri = UriComponentsBuilder.fromHttpUrl(params.getApiUrl())
                .queryParam("KEY", params.getApiKey())
                .queryParam("Type", params.getReturnType())
                .queryParam("pIndex", params.getPageIndex())
                .queryParam("pSize", params.getPageSize())
                .queryParam("STATE_NM", params.getStateNm())
                .encode()
                .build()
                .toUri();

        return restTemplate.getForObject(uri, String.class);
    }

}
