FROM openjdk:17-jdk-alpine
COPY target/oci-streaming-0.0.1-SNAPSHOT.jar oci-streaming.jar
ENTRYPOINT ["java","-jar","/oci-streaming.jar"]