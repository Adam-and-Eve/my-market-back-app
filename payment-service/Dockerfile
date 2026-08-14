FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .

COPY my-market-app/pom.xml ./my-market-app/

COPY payment-service/pom.xml ./payment-service/

COPY api/ ./api/

RUN mvn compile -Dmaven.test.skip=true -B -ntp

COPY payment-service/src ./payment-service/src/

RUN mvn -pl payment-service clean package -Dmaven.test.skip=true -B -ntp

FROM eclipse-temurin:21-jre-noble
WORKDIR /app

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/payment-service/target/payment-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]