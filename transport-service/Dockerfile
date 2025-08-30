# Build via Maven first: mvn -q -pl transport-service -am -DskipTests package
# Then build the image using Docker/Compose
FROM eclipse-temurin:21-jre-alpine

ENV JAVA_OPTS=""

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar

# No HTTP port exposed; service works via messaging
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
