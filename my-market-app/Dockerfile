FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .

COPY payment-service/pom.xml ./payment-service/

COPY my-market-app/pom.xml ./my-market-app/

COPY api/ ./api/

RUN mvn package -DskipTests --fail-never -B -ntp

COPY my-market-app/src ./my-market-app/src/

RUN mvn -pl my-market-app package -DskipTests -B -ntp

FROM eclipse-temurin:21-jre-noble
WORKDIR /app

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/my-market-app/target/my-market-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]