FROM maven:3.9.4-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

RUN find target -name "*.jar" -exec cp {} app.jar \;

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-jar", "app.jar"]
