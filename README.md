# my-market-back-app

Бэкенд приложения «Витрина интернет-магазина»

---

## 🚀 О проекте

Веб-приложение представляет собой полноценную интерактивную витрину интернет-магазина с возможностью поиска, пагинации, фильтрации, управления корзиной покупателя и эмуляцией оформления заказов.

Разработано на платформе **Java 21** с использованием **Spring Boot 4** на современном **асинхронном реактивном стеке технологий (Spring WebFlux)**, что обеспечивает высокую производительность и эффективное использование системных ресурсов при высоких нагрузках.

Интерфейс приложения построен на серверном рендеринге шаблонов **Thymeleaf**, полностью адаптированных под работу с реактивными потоками данных (`Flux` / `Mono`).

---

## 🛠 Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Boot Framework 4+ (Starter WebFlux, Starter Data R2DBC, Thymeleaf)
- **Реактивный движок:** Project Reactor (`Mono`, `Flux`)
- **Сборка:** Maven
- **База данных:** H2 Database (In-Memory хранилище с асинхронным R2DBC-драйвером)
- **Доступ к данным:** Spring Data R2DBC (полностью неблокирующее взаимодействие с БД)
- **Деплой:** Executable JAR (Многоэтапная Docker-контейнеризация, порт 8080)
- **Тестирование:** JUnit 5, Spring Boot Test, WebTestClient (для реактивного тестирования эндпоинтов), Reactor Test (`StepVerifier`)

---

## ⚙️ Конфигурация и запуск

### Переменные окружения (src/main/resources/application.properties)

Приложение настроено на работу с неблокирующей базой данных H2 в оперативной памяти через R2DBC протокол, а также на автоматическую инициализацию схемы при старте:

```bash
spring.application.name=my-market-app

spring.r2dbc.url=r2dbc:h2:mem:///my-market-back-app;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE
spring.r2dbc.username=
spring.r2dbc.password=

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:database/schema.sql
spring.sql.init.data-locations=classpath:database/data.sql
```

### Запуск через Docker

```bash
docker build -t my-market-back-app .
```

```bash
docker run --name my-market-back -p 8080:8080 --rm my-market-back-app
```

---

## 🏗 Локальная сборка и запуск проекта

**# Сборка исполняемого JAR-файла**

```bash
./mvnw clean package
```

**# Локальный запуск бэкенда (без Docker)**

```bash
java -jar target/my-market-back-app-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Тестирование

Запуск полного цикла тестирования (Unit + Integration):

```bash
./mvnw test
```
---

## 🔧 Как внести изменения

- **Создайте новую ветку: git checkout -b feature/название**
- **Внесите изменения**
- **Запустите тесты: ./mvnw test**
- **Соберите проект: ./mvnw clean package**
- **Создайте Pull Request**

---