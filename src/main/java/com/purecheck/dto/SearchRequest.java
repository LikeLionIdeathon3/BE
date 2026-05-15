package com.purecheck.dto;

import lombok.Getter;
import lombok.Setter;

// 프론트엔드에서 받는 요청 데이터 형식
// { "query": "비타민C 1000" } 이런 JSON을 받습니다.
@Getter
@Setter
public class SearchRequest {

    private String query; // 검색할 제품명
}
