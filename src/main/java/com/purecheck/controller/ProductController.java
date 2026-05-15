package com.purecheck.controller;

import com.purecheck.dto.SearchRequest;
import com.purecheck.dto.SearchResponse;
import com.purecheck.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController: 이 클래스가 REST API 요청을 처리한다는 표시
// @RequestMapping: 이 클래스의 모든 API 경로 앞에 /api/product 가 붙습니다
@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // POST /api/product/search 로 들어오는 요청을 처리합니다
    // 프론트에서 { "query": "비타민C 1000" } 형태로 보내면
    // 식약처 API로 검색 후 결과를 돌려줍니다
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> searchProduct(@RequestBody SearchRequest request) {
        log.info("제품 검색 요청: {}", request.getQuery());

        // 빈 검색어 처리
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            SearchResponse errorResponse = SearchResponse.builder()
                    .success(false)
                    .error(SearchResponse.ErrorInfo.builder()
                            .code("INVALID_REQUEST")
                            .message("검색어를 입력해주세요.")
                            .build())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

        SearchResponse response = productService.searchProduct(request.getQuery().trim());
        return ResponseEntity.ok(response);
    }
}
