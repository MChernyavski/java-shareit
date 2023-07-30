FROM amazoncorretto:11-alpine-jdk
#ENV TZ=Asia/Jerusalem
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
