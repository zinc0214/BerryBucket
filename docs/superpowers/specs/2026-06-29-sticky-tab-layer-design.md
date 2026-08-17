---
name: sticky-tab-layer-design
description: MyTabLayer가 스크롤 중 상단에 고정되도록 하는 기능 (LazyColumn + stickyHeader)
metadata:
  type: design
  date: 2026-06-29
  revision: 2
---

# MyTabLayer 스티키 구현 설계 (개정)

## 목표
MyScreen 스크롤 시:
- 아래로 스크롤: MyTopLayer는 위로 스크롤되어 사라지고, MyTabLayer는 상단에 고정(sticky)
- 상단으로 스크롤: MyTopLayer가 다시 노출되고, MyTabLayer도 함께 아래로 내려옴

## 현재 상태 (실패한 첫 시도)
- Column + verticalScroll + offset 방식 사용
- 문제: offset은 시각적 위치만 변경하고 실제 scroll tree 위치는 변하지 않음
- 결과: 탭이 스크롤 중 사라지고, 클릭 이벤트도 작동하지 않음

## 최종 구현 방식: Box 외부 배치 (Column 밖의 절대 위치)

### 아키텍처

**변경 전 (실패들):**
```
Column (verticalScroll)
├── MyTopLayer (스크롤됨)
├── MyTabLayer (Box + offset - 클릭 안 됨 또는 LazyColumn stickyHeader - 터치 안 됨)
└── MyViewPager (스크롤됨)
```

**최종 방식 (Column 외부 배치):**
```
Box (Container - 전체 화면)
├── Column (verticalScroll) - 스크롤 가능
│   ├── MyTopLayer (스크롤되어 위로 사라짐)
│   └── MyViewPager (탭 바 아래에서 스크롤됨)
└── Box (position = Absolute) - 고정 위치
    └── MyTabLayer (상단에 고정, 클릭 정상 작동)
```

### 주요 변경사항

#### 1. Column 구조 재정의
- `Column` 유지 (verticalScroll 유지)
- MyTopLayer + MyViewPager만 포함
- MyTabLayer 제거

```kotlin
Column(
    modifier = Modifier
        .statusBarsPadding()
        .nestedScroll(nestedScrollConnection)
        .verticalScroll(parentScrollState)
) {
    MyTopLayer(...)
    MyViewPager(...)
}
```

#### 2. MyTabLayer를 Box로 외부 배치
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
) {
    // Column (위에서 정의한 Column)
    Column(...) { ... }
    
    // MyTabLayer - 절대 위치로 상단 고정
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .offset(y = profileHeight - minOf(parentScrollState.value.toFloat(), profileHeight.value.toFloat()).dp)
    ) {
        MyTabLayer(...)
    }
}
```

#### 3. 핵심 계산 로직
- MyTabLayer의 y 위치 = profileHeight - min(scrollState, profileHeight)
- scrollState가 profileHeight까지 증가하면: offset이 0으로 됨 (상단 고정)
- scrollState가 0이면: offset이 profileHeight만큼 됨 (원래 위치)

#### 4. 이점
- ✅ MyTabLayer가 Column 밖에 있으므로 클릭이 정상 작동
- ✅ 절대 위치로 배치하므로 진정한 sticky 효과
- ✅ 상단 스크롤 시 자연스럽게 MyTopLayer가 나타남
- ✅ 복잡한 API 의존성 없음 (Compose native만 사용)

### 동작 흐름

1. **초기 상태**: MyTopLayer, MyTabLayer, MyViewPager 모두 표시
2. **아래로 스크롤**: MyTopLayer가 점진적으로 위로 사라짐, MyTabLayer는 상단에 고정
3. **완전히 스크롤**: MyTopLayer 완전히 사라짐, MyTabLayer만 상단에 남음
4. **상단으로 스크롤**: MyTopLayer가 점진적으로 다시 나타남, MyTabLayer와 함께 아래로 내려옴

### 기대 효과

- **진정한 스티키 동작**: LazyColumn의 stickyHeader가 layout tree에서 실제로 고정됨
- **클릭 이벤트 작동**: 탭이 실제로 sticky 위치에 있으므로 클릭 좌표가 정확함
- **부드러운 역스크롤**: 상단 스크롤 시 MyTopLayer가 자연스럽게 다시 나타남
- **HorizontalPager와 호환**: LazyColumn 내의 item으로 pager가 정상 작동

## 주의사항

- ModalBottomSheetLayout은 LazyColumn 외부에 유지
- pagerState 관리는 변경 없음
- 필터링 상태(isFilterUpdated) 전달은 동일
- statusBarsPadding()은 MyTopLayer로 이동

## 테스트 계획

1. **스크롤 다운**: MyTopLayer 사라지고, MyTabLayer는 상단 고정
2. **스크롤 업**: MyTopLayer 다시 나타나고, MyTabLayer와 함께 아래로 내려옴
3. **탭 클릭**: 탭 클릭이 정상 작동
4. **페이저 네비게이션**: 탭 전환과 페이저 이동이 동기화됨
5. **바닥 시트**: 필터 바닥 시트가 정상 작동
