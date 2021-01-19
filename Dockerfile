FROM adoptopenjdk/openjdk11-openj9:alpine AS builder

COPY . .

RUN ./gradlew installDist -Dorg.gradle.daemon=false

FROM adoptopenjdk/openjdk11-openj9:alpine

WORKDIR /usr/app

COPY --from=builder build/install/BankoBot ./

ENTRYPOINT ["/user/app/bin/BankoBot"]
