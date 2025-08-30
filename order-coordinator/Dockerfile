# Build via Maven first: mvn -q -pl order-coordinator -am -DskipTests package
# Then build the image using Docker/Compose
FROM eclipse-temurin:21-jre-alpine

# Optional JVM options can be passed with JAVA_OPTS env
ENV JAVA_OPTS=""

# Copy the fat JAR produced by Spring Boot
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar

# Expose HTTP port for the coordinator
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
