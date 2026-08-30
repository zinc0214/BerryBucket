package com.zinc.waver.ui.presentation.model

/**
 * 닉네임 중복 검사 결과. 가입 2단계와 프로필 수정 화면이 공유한다.
 *
 * 검사한 닉네임을 결과에 함께 담는다. 결과만 들고 있으면 "언젠가 어떤 닉네임이 통과했다"와
 * "지금 입력된 닉네임이 통과했다"를 구분할 수 없어, 닉네임을 바꾼 뒤에도 검사 없이 통과한다.
 */
sealed interface NicknameCheckState {
    data object Idle : NicknameCheckState

    data object Checking : NicknameCheckState

    /** 검사에 성공했고 사용 가능한 닉네임. */
    data class Available(val nickName: String) : NicknameCheckState

    /** 이미 사용 중인 닉네임(6001). */
    data class Duplicated(val nickName: String) : NicknameCheckState

    /** 검사 자체가 실패(네트워크/서버 오류). */
    data object Failed : NicknameCheckState

    /** [nickName] 이 검사를 통과한 상태인지. */
    fun isAvailableFor(nickName: String) = this is Available && this.nickName == nickName

    /** [nickName] 이 중복으로 판정된 상태인지. */
    fun isDuplicatedFor(nickName: String) = this is Duplicated && this.nickName == nickName
}
