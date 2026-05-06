# ==========================================
# 1단계: 빌드 환경 (Builder Stage)
# ==========================================
# JDK(자바 개발 도구)가 포함된 무거운 이미지를 가져와서 빌드만 수행합니다.
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# 1. Gradle Wrapper와 설정 파일들만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 2. 실행 권한 부여 및 의존성 라이브러리 다운로드 (캐싱 활용)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 3. 실제 소스 코드를 복사하고 빌드 진행 (테스트 제외)
COPY src src
RUN ./gradlew build -x test --no-daemon

# ==========================================
# 2단계: 실행 환경 (Runner Stage)
# ==========================================
# JRE(자바 실행 도구)만 포함된 아주 가벼운 이미지를 새로 엽니다.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 4. 앞선 1단계(builder)에서 완성된 .jar 파일만 이 가벼운 환경으로 쏙 가져옵니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 5. Render가 자동으로 주입하는 PORT 환경변수를 사용해 앱을 실행합니다.
# (우리가 application.yml에 ${PORT:8081}로 세팅해 두었기 때문에 완벽히 호환됩니다!)
ENTRYPOINT ["java", "-jar", "app.jar"]
