# 마르투스 · 2026 청년 신약 성서지리

> 교회 청년부의 **터키 · 그리스 성서지리**(2027년 2월 1일 출발)를 함께 준비하는 회원 전용 웹사이트예요.
> 출발 전까지 16주 동안 사도바울의 전도여행을 따라 말씀을 읽고, 체력을 기르고, 조별로 소식을 나눠요.

<br>

## 목차

1. [프로젝트 소개](#1-프로젝트-소개)
2. [개발 환경 · 기술 스택](#2-개발-환경--기술-스택)
3. [주요 기능](#3-주요-기능)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [설치 및 실행 방법](#5-설치-및-실행-방법)
6. [사용 방법](#6-사용-방법)
7. [버전 및 업데이트 정보](#7-버전-및-업데이트-정보)
8. [알려진 문제 · FAQ](#8-알려진-문제--faq)
9. [참고 및 출처 · 저작권](#9-참고-및-출처--저작권)

<br>

## 1. 프로젝트 소개

| 항목 | 내용 |
|---|---|
| 이름 | 마르투스(Martys, 헬라어로 '증인') — 2026 청년 신약 성서지리 |
| 목적 | 답사 참가자들이 출발 전까지 말씀 묵상, 체력 준비, 조별 활동을 한곳에서 함께 하기 |
| 대상 | 성서지리에 참가하는 청년부 회원 (로그인해야 쓸 수 있어요) |
| 참고한 사이트 | [gracestars.kr](https://gracestars.kr) 의 화면 구성과 동작 (구조 분석은 [`docs/gracestars-구조분석.md`](docs/gracestars-구조분석.md)) |

**주요 일정**

| 일정 | 날짜 |
|---|---|
| 말씀 묵상 (16주, 32개 본문) | 2026.10.5 – 2027.1.24 |
| 체력 증진 (매일 체크) | 2026.12.1 – 2027.1.31 |
| 출발 | 2027.2.1 (월) |


## 2. 개발 환경 · 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1, Spring MVC, Spring Data JPA, Spring Security, Bean Validation |
| 화면 | Thymeleaf, Thymeleaf Layout Dialect, Bootstrap 5, 직접 만든 CSS (`style.css`) |
| 지도 | D3.js + Natural Earth 해안선 데이터 (말씀 묵상 지도), Google Maps JavaScript API (여행 지도, 선택) |
| 날씨 | Open-Meteo API (키 필요 없음) |
| 데이터베이스 | MySQL 8 |
| 빌드 | Gradle |
| 기타 | Lombok, Spring Boot DevTools |
| 개발 도구 | IntelliJ IDEA |

<br>

## 3. 주요 기능

### 홈 `/`
- 출발까지 **D-day**, 최근 공지 4개
- **이번 주 말씀** : 사도행전·서신서 본문을 바로 체크
- **체력 증진** : 이번 주 운동 체크 현황
- **묵상 여정 지도** : 바울의 경로 위에 회원들이 지금 어디까지 읽었는지 사진으로 표시

### 말씀 묵상 `/study`
- **16주 한눈에** : 1차·2차·3차 전도여행, 로마 호송 여정별 주차 목록과 전체 지도
- **주차별로 읽기** : 이번 주 경로를 크게 보여주는 지도, 본문 2개 체크, 다음 주차로 이동
- 체크할 때마다 지도에 바울의 경로가 한 걸음씩 이어져요

### 체력 증진 `/fitness`
- 12월·1월 달력에서 날짜를 눌러 운동 체크

### 지도 `/map`
- 답사 일정을 날짜별로 정리하고, 들를 곳을 지도에 표시
- 장소 순서 바꾸기, 다녀온 곳 표시, 장소별 날씨

### 조별 활동 `/group`
- 조원 목록 (조장·리더가 맨 앞), 조별 단체 사진 (조장이 올림)
- 조별 의견 쓰기, 다른 조 의견 보기
- 소속이 **SL · ED** 인 회원은 자동으로 **SLED조**에 들어가요

### 모임 기록 `/moments`
- 사진 여러 장과 함께 모임을 기록하고 월별로 보기

### 공지사항 `/notice`
- 관리자가 공지 작성 (화면에서 바로 쓰기)
- **투표** : 선택지마다 설명 추가, 여러 개 고르기 가능
- 댓글, 새 공지가 있으면 메뉴에 파란 점 표시

### 회의록 `/minutes` 🔒
- 조장·리더와 관리자만 볼 수 있어요
- "기본 틀 넣기" 버튼으로 참석 / 안건 / 정한 것 / 할 일 양식을 한 번에 넣기

### 내 정보 `/me` · 관리자 `/admin`
- 프로필 사진 바꾸기, 로그아웃
- 관리자 : 회원 등록·수정·삭제, 권한(조원 / 조장·리더 / 관리자)과 조 배정

### 권한

| 권한 | 할 수 있는 일 |
|---|---|
| 조원 (`ROLE_USER`) | 기본 기능 모두 |
| 조장·리더 (`ROLE_LEADER`) | + 회의록, 조 단체 사진 올리기 |
| 관리자 (`ROLE_ADMIN`) | + 공지 작성·수정·삭제, 회원 관리 |

### 화면 크기
- 휴대폰부터 넓은 모니터까지 맞춰 보여요
- 넓은 화면에서는 메뉴가 제목 옆 한 줄로 바뀌고(1100px 이상), 홈과 말씀 묵상은 두 칸으로 나뉘어요(900px 이상)

<br>

## 4. 프로젝트 구조

기능마다 패키지를 나누고, 각 패키지 안은 `controller → service → repository → entity` 순서로 같은 구조를 따라요.

```
src/main
├── java/com/mysite/sbb
│   ├── SbbApplication.java
│   ├── config        # 보안 설정, 관리자 계정 자동 생성
│   ├── common        # 일정 상수(TripInfo), 공통 모델, 파일 저장, 마크다운, 좌표 계산
│   ├── member        # 회원가입, 로그인, 내 정보
│   ├── admin         # 회원 관리
│   ├── study         # 말씀 묵상, 바울 여정 지도 데이터
│   ├── fitness       # 체력 증진 달력
│   ├── tripmap       # 여행 지도, 날씨
│   ├── group         # 조별 활동
│   ├── moment        # 모임 기록
│   ├── notice        # 공지사항, 댓글, 투표
│   └── minutes       # 회의록
└── resources
    ├── application.yml
    ├── static        # css, js(journey-map.js, trip-map.js, write-form.js ...), data(지도 데이터)
    └── templates     # Thymeleaf 화면 (layouts, fragments, 기능별 폴더)
```

```
브라우저 ──▶ Spring Security (로그인·권한 확인)
         ──▶ Controller ──▶ Service ──▶ Repository (JPA) ──▶ MySQL
         ◀── Thymeleaf 템플릿으로 그린 HTML
```

<br>

## 5. 설치 및 실행 방법

### 준비물
- JDK 21
- MySQL 8

### 1) 저장소 받기
```bash
git clone https://github.com/honggihyuk/sbb-martys.git
cd sbb-martys
```

### 2) 데이터베이스 만들기
```sql
CREATE DATABASE sbb_a DEFAULT CHARACTER SET utf8mb4;
```
테이블은 처음 실행할 때 JPA가 자동으로 만들어요 (`ddl-auto: update`).

### 3) 설정 확인 — `src/main/resources/application.yml`

| 설정 | 기본값 | 설명 |
|---|---|---|
| `spring.datasource.username / password` | `root` / `rootroot` | 내 MySQL 계정으로 바꿔요 |
| `app.admin.username / password` | `admin` / `admin1234` | 처음 실행할 때 자동으로 만들어지는 관리자 계정 |
| `app.upload-dir` | `uploads` | 올린 사진이 저장되는 폴더 |
| `GOOGLE_MAPS_API_KEY` (환경변수) | 없음 | 있으면 '지도' 메뉴에 Google 지도, 없으면 Natural Earth 지도로 보여줘요 |

> **Google Maps API 키를 쓸 때 주의할 점**
> * **API 키 보호** : Google Cloud Console에서 HTTP referrer 제한을 꼭 걸어두세요. 프론트엔드 코드에 키가 그대로 노출되기 때문이에요.
> * **요금** : Maps Platform은 API 종류(SKU)별로 무료 사용량이 따로 있어요. 위치를 실시간으로 갱신할 때 지도를 다시 불러오지 말고 마커만 옮기면 비용을 줄일 수 있어요.

### 4) 실행
```bash
./gradlew bootRun          # Windows : gradlew.bat bootRun
```
브라우저에서 http://localhost:8080 으로 들어가요.

<br>

## 6. 사용 방법

1. **회원가입** (`/member/signup`) : 이름, 아이디, 비밀번호, 소속(송도·또래·미들·이음·SL·ED), 조를 고르고 가입해요. 이메일은 선택이에요.
2. **관리자 로그인** (`/admin/login`) : `admin` 계정으로 들어가 회원의 권한과 조를 정해요.
3. 매주 **말씀 묵상**에서 본문을 읽고 동그라미를 눌러 체크해요.
4. 12월부터는 **체력 증진** 달력에 매일 운동을 체크해요.
5. 조장은 **조별 활동**에 단체 사진을 올리고, **회의록**을 써요.

<br>

## 7. 버전 및 업데이트 정보

| 버전 | 날짜 | 내용 |
|---|---|---|
| 0.0.1 | 2026.11.16 | GitHub 첫 공개. 홈, 말씀 묵상, 체력 증진, 지도, 조별 활동, 모임 기록, 공지사항(투표), 회의록, 관리자 |

<br>

## 8. 알려진 문제 · FAQ

**Q. 로그인하지 않으면 아무 화면도 안 보여요.**
회원 전용 사이트라서 회원가입·로그인 화면만 열려 있어요.

**Q. '지도' 메뉴에 Google 지도가 안 나와요.**
`GOOGLE_MAPS_API_KEY` 환경변수가 없으면 Natural Earth 지도로 대신 보여줘요. IntelliJ에서는 Run > Edit Configurations > Environment variables 에 넣어요.

**Q. 회의록 메뉴를 누르면 막혀요.**
조장·리더와 관리자만 볼 수 있어요. 관리자 화면에서 권한을 바꿔 주세요.

**Q. 소속을 SL이나 ED로 고르면 조를 고를 수 없어요.**
SL·ED 회원은 자동으로 SLED조에 들어가고, 조장 대신 '리더'로 표시돼요.

**알려진 문제**
- 고대 지명의 정확한 위치와 일부 여정·서신서의 기록 시점에는 학자들 사이에 견해 차이가 있어요.
- 버그나 제안은 [Issues](https://github.com/honggihyuk/sbb-martys/issues)에 남겨 주세요.

<br>

## 9. 참고 및 출처 · 저작권

- 화면 구성 참고 : [gracestars.kr](https://gracestars.kr)
- 해안선 지도 데이터 : [Natural Earth](https://www.naturalearthdata.com) (퍼블릭 도메인)
- 지도 그리기 : [D3.js](https://d3js.org) (ISC License)
- 화면 스타일 : [Bootstrap](https://getbootstrap.com) (MIT License)
- 날씨 : [Open-Meteo](https://open-meteo.com) (CC BY 4.0)
- 학습 교재 : 「점프 투 스프링부트」 SBB 프로젝트 구조

이 프로젝트는 교회 청년부 성서지리 준비와 대학 수업(스프링 프레임워크) 학습을 위해 만들었어요.
