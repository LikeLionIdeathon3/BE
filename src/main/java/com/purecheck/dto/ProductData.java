package com.purecheck.dto;

import lombok.Builder;
import lombok.Getter;

// 제품 정보를 담는 데이터 클래스
// 프론트엔드로 보내는 응답의 "data" 부분입니다.
@Getter
@Builder
public class ProductData {

    private String productName;      // 제품명
    private String brand;            // 업체(브랜드)명
    private String category;         // 품목 대분류 (예: 건강기능식품, 기타가공품)
    private String subCategory;      // 품목 소분류 (예: 비타민 및 무기질, 캔디류)
    private Boolean isHealthFood;    // 건강기능식품 여부 (true/false/null)
    private String approvedFunction; // 식약처 인정 기능성 내용 (건강기능식품만 해당)
    private String warning;          // 경고 메시지 (가짜 영양제일 때)
    private String source;           // 출처 (식약처 품목제조 DB)
    private String checkedAt;        // 조회 날짜
    private boolean notFound;        // DB 미등록 여부
}
