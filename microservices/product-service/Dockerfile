FROM openjdk:12.0.2

EXPOSE 8080

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

#CONSTRUCIION
#docker build --tag product-service .
#Ejeccucion
# docker run --rm -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-service