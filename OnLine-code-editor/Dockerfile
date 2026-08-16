#FROM ubuntu:latest
#LABEL authors="windows11"
#
#ENTRYPOINT ["top", "-b"]
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY Main.java .

RUN javac Main.java

CMD ["java", "Main"]