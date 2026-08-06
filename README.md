# my-market-back-app

Мультимодульный бэкенд приложения **«Витрина интернет-магазина»** с неблокирующим кешированием в Redis и интеграцией с сервисом платежей через OpenAPI.

---

## 🚀 О проекте

Проект представляет собой реактивное мультимодульное решение, состоящее из основного веб-приложения интернет-магазина и отдельного RESTful-сервиса платежей.

---

## 🛠 Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Boot 4+ (Spring WebFlux, Spring Data R2DBC, Spring Data Redis Reactive, Thymeleaf)
- **Реактивный движок:** Project Reactor (`Mono`, `Flux`)
- **Сборка:** Maven
- **Оркестрация и деплой** Docker, Docker Compose, Netty Embedded Server
- **Кеширование** Redis
- **Контракты и API** OpenAPI 3.0 Spec + OpenAPI Generator Maven Plugin
- **База данных:** H2 Database (In-Memory хранилище с асинхронным R2DBC-драйвером)
- **Доступ к данным:** Spring Data R2DBC (полностью неблокирующее взаимодействие с БД)
- **Деплой:** Executable JAR (Многоэтапная Docker-контейнеризация)
- **Тестирование:** JUnit 5, Spring Boot Test, WebTestClient (для реактивного тестирования эндпоинтов), Reactor Test (`StepVerifier`)

---

## ⚙️ Конфигурация и переменные окружения

Приложение настроено на работу с неблокирующей базой данных H2 в оперативной памяти через R2DBC протокол, а также на автоматическую инициализацию схемы при старте:

Основные параметры системы настраиваются через application.properties модулей или переопределяются переменными окружения:

### Переменные окружения модуля **my-market-app** (application.properties)

```bash
spring.application.name=my-market-app

spring.r2dbc.url=r2dbc:h2:mem:///my-market-back-app;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE
spring.r2dbc.username=
spring.r2dbc.password=

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:database/schema.sql
spring.sql.init.data-locations=classpath:database/data.sql

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.repositories.enabled=false

app.items-cache.ttl=2m
app.payment-service.base-url=http://localhost:8081

server.port=8080
```

### Переменные окружения модуля **payment-service** (application.properties)

```bash
spring.application.name=payment-service

payment.initial-balance=1000000

server.port=8081
```

## 🐳 Запуск через Docker Compose

Самый быстрый способ развернуть весь комплекс со всеми зависимостями (Redis + Payment Service + Main App):

Сборка и запуск всех сервисов:

```bash
docker-compose up --build -d
```

Проверка статуса запущенных контейнеров:

```bash
docker-compose ps
```

Просмотр логов:

```bash
docker-compose logs -f
```

Остановка и удаление контейнеров:

```bash
docker-compose down
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