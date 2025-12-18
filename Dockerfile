# 1. 자바 17 JDK 슬림 버전 사용
FROM eclipse-temurin:17-jdk-focal

# 2. 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너 내부로 복사
# STS4 터미널에서 ./gradlew build 실행 후 생성되는 jar 파일을 대상으로 합니다.
COPY *-SNAPSHOT.jar app.jar

# 4. 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]