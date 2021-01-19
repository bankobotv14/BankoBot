FROM adoptopenjdk/openjdk11-openj9:alpine AS builder

COPY docsdex .

RUN ./gradlew shadowJar -Dorg.gradle.daemon=false

FROM adoptopenjdk/openjdk11-openj9:alpine

WORKDIR /usr/app

COPY --from=builder build/libs/docdex.jar ./

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/app/docdex.jar"]
