# ================================
# 1단계: 빌드 스테이지
# ================================
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 의존성 파일만 먼저 복사 → 의존성 캐싱 (소스 변경 시 의존성 재다운로드 방지)
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true

# 전체 소스 복사 후 빌드 (테스트 코드 컴파일 및 실행 제외)
COPY . .
RUN gradle assemble --no-daemon

# ================================
# 2단계: 실행 스테이지
# ================================
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 보안 강화: root 대신 일반 유저로 실행
RUN useradd -ms /bin/bash appuser
USER appuser

# 컨테이너 포트 명시 (실제 포트 오픈은 docker-compose 또는 AWS에서 설정)
EXPOSE 8080

# 실행 명령
# -Dspring.profiles.active=prod : 운영 환경 프로파일 적용
# -XX:+UseContainerSupport : JVM이 컨테이너 메모리 인식하도록 설정
# -XX:MaxRAMPercentage=75.0 : 컨테이너 메모리의 75%만 JVM에 할당
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]