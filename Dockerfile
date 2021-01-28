FROM adoptopenjdk/openjdk15-openj9 as builder

COPY . .

RUN ./gradlew --no-deamon installDist

FROM adoptopenjdk/openjdk15-openj9

WORKDIR /user/app

COPY --from=builder build/install/BankoBot ./

ENTRYPOINT ["/user/app/bin/BankoBot"]