FROM adoptopenjdk/openjdk11:alpine-jre

# Установка PostgreSQL
RUN apk update && apk add postgresql

# Копирование JAR-файла сервиса платежей
COPY target/paymentService-0.0.1-SNAPSHOT.jar paymentService.jar

# Установка переменных окружения для настройки PostgreSQL
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=1234
ENV POSTGRES_DB=postgres

# Открытие порта для сервиса платежей
EXPOSE 8080

# Запуск службы PostgreSQL и сервиса платежей при старте контейнера
CMD pg_ctl start && java -jar paymentService.jar
