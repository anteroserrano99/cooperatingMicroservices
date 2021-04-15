FROM openjdk:12.0.2

EXPOSE 8080

ADD ./build/libs/*.jar app.jar


ENTRYPOINT ["java", "-jar", "/app.jar"]

# docker build --tag product-composite-service .
# docker run --rm -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name composite-srv product-composite-service