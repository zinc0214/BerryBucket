# MyTabLayer 스티키 구현 계획 (최종 버전)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** MyScreen 스크롤 시 MyTabLayer가 상단에 고정되고, 상단으로 스크롤할 때 함께 내려오는 스티키 탭 구현 (Box 외부 배치 방식)

**Architecture:** Column을 감싸는 외부 Box를 생성하고, MyTabLayer를 절대 위치로 상단에 배치. 이를 통해 진정한 스티키 효과와 정상적인 클릭 이벤트 처리 달성.

**Tech Stack:** Kotlin, Jetpack Compose, Box alignment, offset modifier, rememberScrollState

## Global Constraints

- ModalBottomSheetLayout은 외부 Box 외부에 유지
- pagerState 관리는 변경 없음
- 필터링 상태(isFilterUpdated) 전달 동일
- 기본 스크롤 구조 유지

---

### Task 1: Box 외부 배치로 MyTabLayer 구현

**Files:**
- Modify: `feature/ui-my/src/main/java/com/zinc/waver/ui_my/MyScreen.kt:171-235`

**Interfaces:**
- Consumes: `tabItems` (List<MyTabType>), `pagerState` (PagerState), `profileHeight`, `parentScrollState`
- Produces: 외부 Box로 MyTabLayer가 절대 위치에서 sticky로 표시됨

**변경 사항:**
- Column을 Box로 감싸기
- MyTabLayer를 Column에서 제거하고 Box 내 절대 위치에 배치
- offset 계산으로 스크롤 시 위치 제어

- [ ] **Step 1: 현재 구조 확인**

현재 ModalBottomSheetLayout 내부 구조 (lines 171-234):
```kotlin
ModalBottomSheetLayout(...) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(parentScrollState)
    ) {
        MyTopLayer(...)
        
        Box(...) { MyTabLayer(...) }  // 제거할 부분
        
        MyViewPager(...)
    }
}
```

- [ ] **Step 2: 외부 Box 구조 생성**

변경 후:
```kotlin
ModalBottomSheetLayout(...) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(parentScrollState)
        ) {
            MyTopLayer(...)
            MyViewPager(...)
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .align(Alignment.TopCenter)
                .offset(
                    y = (profileHeight.value - minOf(
                        parentScrollState.value.toFloat(),
                        profileHeight.value.toFloat()
                    ).dp).dp
                )
        ) {
            MyTabLayer(tabItems, pagerState, coroutineScope)
        }
    }
}
```

- [ ] **Step 3: offset 계산 로직 검증**

offset 계산:
- profileHeight - min(scrollState.value, profileHeight.value)
- scrollState = 0: offset = profileHeight (원래 위치)
- scrollState = profileHeight: offset = 0 (상단 고정)
- scrollState > profileHeight: offset = 0 (계속 고정)

- [ ] **Step 4: MyViewPager 높이 확인**

MyViewPager의 pagerHeight 계산이 Column 변경으로 영향받지 않는지 확인
- pagerHeight = screenHeight - profileHeight
- 계산 로직은 그대로 유지

- [ ] **Step 5: 빌드 확인**

```bash
cd /Users/zinc/Android/Waver
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add feature/ui-my/src/main/java/com/zinc/waver/ui_my/MyScreen.kt
git commit -m "feat: implement sticky tabs with external Box positioning

- Wrap Column in outer Box for absolute positioning
- Move MyTabLayer outside Column with calculated offset
- offset = profileHeight - min(scrollState, profileHeight)
- Tabs now truly sticky and click events work properly
- No dependency on Compose stickyHeader API
"
```

---

### Task 2: 앱 실행 및 동작 검증

**Files:**
- Test: 수동 테스트 (앱 실행)

**동작 확인 체크리스트:**

- [ ] **Step 1: 앱 빌드 및 설치**

```bash
cd /Users/zinc/Android/Waver
./gradlew installDebug
```

Expected: 앱이 디바이스/에뮬레이터에 설치됨

- [ ] **Step 2: MyScreen 진입**

앱을 실행하고 MyScreen으로 진입 (하단 네비게이션 My 탭)

- [ ] **Step 3: 아래로 스크롤 테스트**

화면을 아래로 스크롤합니다.
**기대 결과:**
- MyTopLayer (프로필 정보)가 위로 스크롤되어 사라짐
- MyTabLayer (ALL, CATEGORY, DDAY)가 상단에 고정되어 남음
- 상단 고정된 탭 바 아래로 콘텐츠가 스크롤됨

- [ ] **Step 4: 상단으로 스크롤 테스트**

화면을 상단으로 스크롤합니다.
**기대 결과:**
- MyTopLayer가 다시 나타남
- MyTabLayer가 함께 아래로 내려옴
- 스크롤 위치가 원래대로 복원됨

- [ ] **Step 5: 탭 클릭 테스트** ⭐ **CRITICAL**

탭 바에서 다른 탭(CATEGORY, DDAY)을 클릭합니다.
**기대 결과:**
- ✅ 탭 클릭이 정상적으로 작동 (이전 시도들의 주요 실패점)
- ✅ 탭이 정상적으로 전환됨
- ✅ 선택된 탭 표시(언더라인)가 정상 작동
- ✅ 페이저가 올바른 페이지로 이동

- [ ] **Step 6: 필터 바닥 시트 테스트**

ALL 탭에서 필터 아이콘을 클릭합니다.
**기대 결과:**
- 바닥 시트가 정상적으로 열림
- MyTabLayer가 여전히 보임
- 필터 적용 후 닫혀도 레이아웃 정상

- [ ] **Step 7: 검증 완료**

모든 동작이 정상이면 구현 완료입니다.

---

## 테스트 계획

1. **스크롤 동작 검증:**
   - 아래로 스크롤할 때 MyTopLayer가 완전히 사라지는가?
   - MyTabLayer가 상단에 고정되어 있는가?

2. **역스크롤 검증:**
   - 상단으로 스크롤할 때 MyTopLayer가 다시 나타나는가?
   - MyTabLayer가 함께 아래로 내려오는가?

3. **탭 기능 검증:** ⭐ **가장 중요**
   - 탭 클릭이 정상 작동하는가? (이전 실패점)
   - 페이저 전환이 정상인가?
   - 선택 표시가 정상인가?

4. **UI 요소 상호작용 검증:**
   - 바닥 시트가 정상 작동하는가?
   - 기타 UI 요소가 영향받지 않는가?

---

## 구현 노트

**이 방식의 핵심:**
- MyTabLayer가 Column 외부의 Box로 배치됨
- Box의 alignment와 offset으로 위치 제어
- 클릭 이벤트는 Column의 영향을 받지 않음
- 스크롤 상태(parentScrollState)에 따라 offset 계산

**타입 변환 주의:**
- `profileHeight.value`: Dp 타입
- `parentScrollState.value`: Float (픽셀)
- 계산 후 `.dp`로 변환하여 offset에 적용

**장점:**
- Native Compose API만 사용
- 버전 호환성 좋음
- 구현이 단순하고 명확함
- 클릭 이벤트 정상 작동
