FROM adpoptopenjdk/openjdk11-openj9 AS builder

COPY docsdex .

RUN ./gradlew shadowJar -Dorg.gradle.daemon=false

FROM adpoptopenjdk/openjdk11-openj9

WORKDIR /usr/app

COPY --from=builder build/libs/docdex.jar ./

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/app/docdex.jar"]
