FROM openjdk:17-jdk-slim AS build

WORKDIR /build

COPY gradlew .
COPY gradle ./gradle

COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true

COPY src ./src

RUN ./gradlew build --no-daemon -x test

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /build/build/libs/workchat-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]