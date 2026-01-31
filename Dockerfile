# ---------- builder ----------
FROM gradle:8-jdk17 AS builder
WORKDIR /app
COPY . .

# bootJar를 만들고(스프링부트 실행 jar), 그 중 1개를 app.jar로 고정
RUN gradle bootJar -x test --no-daemon && \
    set -e; \
    JAR="$(ls -1 /app/build/libs/*.jar | head -n 1)"; \
    echo "Using jar: $JAR"; \
    cp "$JAR" /app/app.jar

# ---------- runner ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 이제는 와일드카드 없이 단일 파일만 복사
COPY --from=builder /app/app.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
