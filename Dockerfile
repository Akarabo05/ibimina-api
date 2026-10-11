FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 ibimina
COPY --from=build /app/target/*.jar app.jar
USER ibimina
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]