package com.purecheck.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.purecheck.dto.ProductData;
import com.purecheck.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${foodsafety.api.key}")
    private String apiKey;

    @Value("${foodsafety.api.base-url}")
    private String baseUrl;

    @Value("${foodsafety.api.health-food-service}")
    private String healthFoodService;  // I2710

    @Value("${foodsafety.api.general-food-service}")
    private String generalFoodService; // I1250

    private String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public SearchResponse searchProduct(String query) {
        try {
            // 1단계: 건강기능식품 DB(I2710)에서 검색
            ProductData healthFoodResult = searchInHealthFoodDb(query);
            if (healthFoodResult != null) {
                return SearchResponse.builder().success(true).data(healthFoodResult).build();
            }

            // 2단계: 식품(첨가물)품목제조보고 DB(I1250)에서 검색
            ProductData generalFoodResult = searchInGeneralFoodDb(query);
            if (generalFoodResult != null) {
                return SearchResponse.builder().success(true).data(generalFoodResult).build();
            }

            // 3단계: 두 DB 모두에 없는 경우 → 미등록 제품
            return SearchResponse.builder()
                    .success(true)
                    .data(ProductData.builder()
                            .productName(query)
                            .brand(null).category(null).subCategory(null)
                            .isHealthFood(null).approvedFunction(null).warning(null)
                            .source(null).checkedAt(today()).notFound(true)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("식약처 API 호출 중 오류 발생: {}", e.getMessage(), e);
            return SearchResponse.builder()
                    .success(false)
                    .error(SearchResponse.ErrorInfo.builder()
                            .code("API_ERROR")
                            .message("식약처 API 호출 중 오류가 발생했습니다.")
                            .build())
                    .build();
        }
    }

    // 건강기능식품 DB(I2710) 검색
    // 식약처 API는 파라미터를 쿼리스트링이 아닌 경로에 /변수명=값 형식으로 붙입니다
    private ProductData searchInHealthFoodDb(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = baseUrl + "/" + apiKey + "/" + healthFoodService + "/json/1/5/PRDLST_NM=" + encoded;

        log.info("건강기능식품 DB 검색 URL: {}", url);
        String responseBody = restTemplate.getForObject(url, String.class);
        log.info("건강기능식품 DB 응답: {}", responseBody);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode serviceNode = root.get(healthFoodService); // "I2710" 키로 접근

        if (serviceNode == null) return null;

        String totalCount = serviceNode.path("total_count").asText("0");
        if ("0".equals(totalCount)) return null;

        JsonNode firstRow = serviceNode.path("row").get(0);
        if (firstRow == null) return null;

        // 반환된 제품명이 검색어를 포함하지 않으면 무관한 결과로 판단하고 무시
        String productName = firstRow.path("PRDLST_NM").asText(null);
        if (productName == null || !productName.contains(query)) return null;

        String fnclty = firstRow.path("FNCLTY_CNTNT").asText(null);
        if (fnclty != null && fnclty.length() > 200) {
            fnclty = fnclty.substring(0, 200) + "...";
        }

        return ProductData.builder()
                .productName(productName)
                .brand(firstRow.path("BSSH_NM").asText(null))
                .category("건강기능식품")
                .subCategory(firstRow.path("PRDLST_CD_NM").asText(null))
                .isHealthFood(true)
                .approvedFunction(fnclty)
                .warning(null)
                .source("식약처 품목제조 DB")
                .checkedAt(today())
                .notFound(false)
                .build();
    }

    // 식품(첨가물)품목제조보고 DB(I1250) 검색
    private ProductData searchInGeneralFoodDb(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = baseUrl + "/" + apiKey + "/" + generalFoodService + "/json/1/5/PRDLST_NM=" + encoded;

        log.info("일반식품 DB 검색 URL: {}", url);
        String responseBody = restTemplate.getForObject(url, String.class);
        log.info("일반식품 DB 응답: {}", responseBody);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode serviceNode = root.get(generalFoodService); // "I1250" 키로 접근

        if (serviceNode == null) return null;

        String totalCount = serviceNode.path("total_count").asText("0");
        if ("0".equals(totalCount)) return null;

        JsonNode firstRow = serviceNode.path("row").get(0);
        if (firstRow == null) return null;

        // 반환된 제품명이 검색어를 포함하지 않으면 무관한 결과로 판단하고 무시
        String productName = firstRow.path("PRDLST_NM").asText(null);
        if (productName == null || !productName.contains(query)) return null;

        String category = firstRow.path("PRDLST_DCNM").asText("일반식품");
        String subCategory = firstRow.path("PRDLST_CD_NM").asText(null);
        String warning = buildWarningMessage(category, subCategory);

        return ProductData.builder()
                .productName(productName)
                .brand(firstRow.path("BSSH_NM").asText(null))
                .category(category)
                .subCategory(subCategory)
                .isHealthFood(false)
                .approvedFunction(null)
                .warning(warning)
                .source("식약처 품목제조 DB")
                .checkedAt(today())
                .notFound(false)
                .build();
    }

    private String buildWarningMessage(String category, String subCategory) {
        if (subCategory != null && !subCategory.isBlank()) {
            return String.format(
                    "식약처 가이드라인에 따르면, 이 제품은 건강기능식품이 아닌 %s(%s)으로 분류되어 있습니다.",
                    category, subCategory);
        }
        return String.format(
                "식약처 가이드라인에 따르면, 이 제품은 건강기능식품이 아닌 %s으로 분류되어 있습니다.",
                category);
    }
}
