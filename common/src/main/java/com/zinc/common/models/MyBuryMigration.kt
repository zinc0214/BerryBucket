package com.zinc.common.models

// 마이버리 데이터 이관 요청 결과.
// 이관은 서버 스케줄러가 비동기로 처리하므로 앱은 요청 접수 결과만 알 수 있다.
enum class MyBuryMigrationResult {
    REQUESTED,          // 이관 요청 접수됨
    ALREADY_DONE,       // 8201 - 이미 이관 완료
    ALREADY_REQUESTED,  // 8202 - 이미 이관 요청됨
    NONE;               // 8200 / 그 외 코드 / 네트워크 오류 - 안내 없이 진행

    companion object {
        fun from(response: CommonResponse2): MyBuryMigrationResult = when {
            response.success -> REQUESTED
            response.code == "8201" -> ALREADY_DONE
            response.code == "8202" -> ALREADY_REQUESTED
            else -> NONE
        }
    }
}
