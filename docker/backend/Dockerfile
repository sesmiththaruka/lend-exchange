FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17
ARG PROFILE=dev
ARG APP_VERSION=1.0.0

WORKDIR /app
COPY --from=build /build/target/lend-exchange-*.jar /app/

EXPOSE 8088

ENV DB_URL = jdbc:postgresql://postgres-sql-le:5432/lend_ex
ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

CMD java -jar -Dspring-profiles.active=${ACTIVE_PROFILE} -Dspring.datasource.url=${DB_URL} lend-exchange-${JAR_VERSION}.jar
#20:29