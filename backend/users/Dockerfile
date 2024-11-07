FROM eclipse-temurin:21-alpine
# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar
COPY . ./
ENV SPRING_PROFILES_ACTIVE=prod
# Ensure mvnw has LF line endings
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw clean install -DskipTests
EXPOSE 8081
ENTRYPOINT ["java","-jar","target/kickoff-tournament-management-users-0.0.1-SNAPSHOT.jar"]
