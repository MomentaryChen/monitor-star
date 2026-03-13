FROM eclipse-temurin:8-jre-alpine
WORKDIR /app

# 預設使用已在主機上 build 好的 jar
# 請先在專案內執行：mvn clean package -DskipTests
COPY target/order-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java -DINSTANCE_IP=$(hostname -i) -jar /app/app.jar"]

