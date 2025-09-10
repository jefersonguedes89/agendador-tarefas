FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/agendador-tarefas-0.0.1-SNAPSHOT.jar /app/agendador.jar

CMD ["java", "-jar", "/app/agendador.jar"]