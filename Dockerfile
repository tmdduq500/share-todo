# 1단계: Gradle을 이용하여 빌드
# Gradle 8 + JDK 17 이미지
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# 프로젝트 소스 전체를 컨테이너로 복사
COPY . .

# Gradle 빌드 (테스트 생략 가능)
RUN gradle build -x test --no-daemon

# 2단계: 경량 JRE 이미지를 이용하여 실행 환경 구성
# JDK 17 기반 경량 JRE 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 산출물 Jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너에서 열어줄 포트
EXPOSE 8080

# 스프링 부트 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
