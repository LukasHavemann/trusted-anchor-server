FROM openjdk:11
COPY /build/libs/*.jar .
CMD ["java", "-jar", "trusted-anchor-server-0.0.1-SNAPSHOT.jar"]