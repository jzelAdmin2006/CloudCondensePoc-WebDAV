# Build
FROM gradle:jdk17 AS build
WORKDIR /app
COPY . /app
RUN ./gradlew build

# Runtime
FROM eclipse-temurin:17

RUN apt update && apt install -y p7zip-full

RUN mkdir /work
ENV TMP_ARCHIVE_WORK_DIR=/work

WORKDIR /app
COPY --from=build /app/build/libs/app.jar .
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
