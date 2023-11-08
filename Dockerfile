FROM openjdk:8-jdk-alpine
WORKDIR app
ARG ARTIFACT_LOCATION=target
ARG ARTIFACT_NAME=common-security-rbac-service-3.0.0-SNAPSHOT.jar
COPY ${ARTIFACT_LOCATION}/${ARTIFACT_NAME} /app/${ARTIFACT_NAME}
EXPOSE 8094
ENTRYPOINT sh -c "java -jar -Djdk.io.File.enableADS=true -Dserver.profile=dev /app/common-security-rbac-service-3.0.0-SNAPSHOT.jar"
