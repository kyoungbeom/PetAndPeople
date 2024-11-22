package com.ssafy.petandpeople.application.service.adoption;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ssafy.petandpeople.application.dto.adoption.ApiRequestParams;
import com.ssafy.petandpeople.application.dto.adoption.AdoptionDto;
import com.ssafy.petandpeople.application.dto.adoption.ErrorResponseDto;
import com.ssafy.petandpeople.common.exception.api.ErrorResponseException;
import com.ssafy.petandpeople.infrastructure.external.JsonParser;
import com.ssafy.petandpeople.infrastructure.external.ExternalApiClient;
import com.ssafy.petandpeople.infrastructure.persistence.repository.RedisRepository;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdoptionService {

    private static final int PAGE_SIZE = 15;
    private static final Dotenv DOTENV = Dotenv.load();
    private static final String API_URL = "https://openapi.gg.go.kr/AbdmAnimalProtect";
    private static final String ADOPTION_REDIS_KEY = DOTENV.get("ADOPTION_REDIS_KEY");

    private final JsonParser jsonParser;
    private final RedisRepository redisRepository;
    private final ExternalApiClient externalApiClient;

    public AdoptionService(JsonParser jsonParser, RedisRepository redisRepository, ExternalApiClient externalApiClient) {
        this.jsonParser = jsonParser;
        this.redisRepository = redisRepository;
        this.externalApiClient = externalApiClient;
    }

    @PostConstruct
    public void initialize() {
        List<AdoptionDto> adoptionDataCluster = new ArrayList<>();

        for (int pageIndex = 1; pageIndex <= 2; pageIndex++) {
            ApiRequestParams params = new ApiRequestParams(API_URL, pageIndex, 1000);
            String response = externalApiClient.requestApi(params);

            validateResponse(response);

            String jsonData = jsonParser.extractData(response, "/AbdmAnimalProtect/1/row");

            List<AdoptionDto> adoptionData = jsonParser.jsonToDtoList(jsonData, new TypeReference<List<AdoptionDto>>() {});

            adoptionDataCluster.addAll(adoptionData);
        }

        redisRepository.save(ADOPTION_REDIS_KEY, adoptionDataCluster);
    }

    public List<AdoptionDto> getAdoptionData(int pageNum) {
        Object value = redisRepository.find(ADOPTION_REDIS_KEY);

        List<AdoptionDto> adoptionDataCluster = jsonParser.convertToType(value, new TypeReference<List<AdoptionDto>>() {});

        return getPageSlice(adoptionDataCluster, pageNum);
    }

    private void validateResponse(String response) {
        ErrorResponseDto error = jsonParser.extractErrorCode(response);

        if(hasError(error)) {
            throw new ErrorResponseException("<API 응답 에러 발생> " + "CODE:" + error.getCode() + " MESSAGE: " + error.getMessage());
        }
    }

    private Boolean hasError(ErrorResponseDto error) {
        return error != null;
    }

    private <T> List<T> getPageSlice(List<T> dataCluster, int pageNum) {
        int start = Math.max(0, (pageNum - 1) * PAGE_SIZE);
        int end = Math.min(start + PAGE_SIZE, dataCluster.size());

        return dataCluster.subList(start, end);
    }

}