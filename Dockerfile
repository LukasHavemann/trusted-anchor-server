FROM openjdk:11
COPY /build/libs/*.jar .
COPY src/main/resources/myCA.key src/main/resources/myCA.key
COPY src/main/resources/rootCA.pem src/main/resources/rootCA.pem
CMD ["java", "-jar", "trusted-anchor-server-0.0.1-SNAPSHOT.jar"]