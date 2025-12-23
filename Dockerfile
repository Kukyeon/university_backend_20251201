FROM eclipse-temurin:17-jdk-focal

# 💡 OS 레벨에서 타임존을 서울로 설정
ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

WORKDIR /app
COPY university.jar app.jar

# 💡 실행 시 타임존 옵션을 명시적으로 추가
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]