cd ../microservices

cd product-composite-service && gradlew bootJar && cd .. && cd product-service && gradlew bootJar && cd .. && cd review-service && gradlew bootJar && cd .. && cd recommendation-service && gradlew bootJar && cd .. && cd .. && cd api && gradlew bootJar && cd ../spring-cloud/eurekaserver && gradlew bootJar cd ../gateway && gradlew bootJar && cd ../..