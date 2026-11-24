FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -q clean package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /build/target/cards4play-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

RUN mkdir -p /app/datos

ENV TASA_DEFECTO=4000
ENV PORT=4567

EXPOSE 4567

CMD ["java", "-jar", "app.jar"]