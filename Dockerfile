# ---- Build Stage ----
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Gradle wrapper とビルドファイルを先にコピー（依存関係キャッシュ用）
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# ソースをコピーしてビルド
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# ---- Runtime Stage ----
FROM eclipse-temurin:25-jre

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates tzdata && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Tokyo

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
