package com.purecheck.dto;

import lombok.Builder;
import lombok.Getter;

// 프론트엔드로 보내는 최종 응답 클래스
// { "success": true, "data": {...} } 또는
// { "success": false, "error": {...} } 형태입니다.
@Getter
@Builder
public class SearchResponse {

    private boolean success;    // 요청 성공 여부
    private ProductData data;   // 성공 시 제품 데이터
    private ErrorInfo error;    // 실패 시 에러 정보

    // 에러 정보를 담는 내부 클래스
    @Getter
    @Builder
    public static class ErrorInfo {
        private String code;    // 에러 코드 (예: API_ERROR)
        private String message; // 에러 메시지
    }
}
