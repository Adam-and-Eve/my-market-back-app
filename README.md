# my-market-back-app

Мультимодульный бэкенд приложения **«Витрина интернет-магазина»** с неблокирующим кешированием товаров в Redis, интеграцией с сервисом платежей через OpenAPI и OAuth2/OIDC аутентификацией через Keycloak.

---

## 🚀 О проекте

Проект представляет собой реактивное мультимодульное решение:

- Основное веб-приложение интернет-магазина;
- Отдельный RESTful-сервис платежей;
- Redis для кеширования данных товаров;
- Keycloak для управления пользователями и OAuth2/OIDC аутентификации.

---

## 🛠 Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Boot 4+
- **Web:** Spring WebFlux, Thymeleaf
- **Реактивный стек:** Project Reactor (`Mono`, `Flux`)
- **Доступ к данным:** Spring Data R2DBC
- **Кеширование:** Spring Data Redis Reactive
- **Безопасность:** Spring Security OAuth2 Client, OAuth2 Resource Server, Keycloak
- **API-контракт:** OpenAPI 3 + OpenAPI Generator
- **База данных:** H2 Database (R2DBC)
- **Сборка:** Maven
- **Контейнеризация:** Docker, Docker Compose
- **Сервер:** Embedded Netty
- **Тестирование:** JUnit 5, Spring Boot Test, WebTestClient, Reactor Test

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

app.security.keycloak.logout-uri=${KEYCLOAK_LOGOUT_URI:http://localhost:8082/realms/my-market/protocol/openid-connect/logout}

spring.security.oauth2.client.registration.keycloak.provider=keycloak
spring.security.oauth2.client.registration.keycloak.client-id=${KEYCLOAK_LOGIN_CLIENT_ID}
spring.security.oauth2.client.registration.keycloak.client-secret=${KEYCLOAK_LOGIN_CLIENT_SECRET}
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.keycloak.scope[0]=openid

spring.security.oauth2.client.provider.keycloak.authorization-uri=${KEYCLOAK_AUTHORIZATION_URI:http://localhost:8082/realms/my-market/protocol/openid-connect/auth}
spring.security.oauth2.client.provider.keycloak.token-uri=${KEYCLOAK_TOKEN_URI:http://localhost:8082/realms/my-market/protocol/openid-connect/token}
spring.security.oauth2.client.provider.keycloak.jwk-set-uri=${KEYCLOAK_JWK_SET_URI:http://localhost:8082/realms/my-market/protocol/openid-connect/certs}
spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username

server.port=8080
```

### Переменные окружения модуля **payment-service** (application.properties)

```bash
spring.application.name=payment-service

payment.initial-balance=1000000

spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8082/realms/my-market
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8082/realms/my-market/protocol/openid-connect/certs

server.port=8081
```
## 🔐 Аутентификация

Пользователи приложения находятся в Keycloak.

Приложение использует OAuth2/OIDC:

- пользователь входит через Keycloak;
- после успешной авторизации приложение получает JWT;
- локально хранится только профиль пользователя, необходимый приложению.

Пароли и учетные данные пользователей находятся в Keycloak.

# 💳 Интеграция платежей

Обмен между market-app и payment-service выполняется через JSON API.

OpenAPI спецификация используется для генерации:

- реактивного WebClient-клиента в market-app;
- реактивных серверных интерфейсов в payment-service.

Платежный сервис предоставляет:

- получение текущего баланса;
- выполнение платежа.

При успешной оплате создаётся заказ.
При недостаточном балансе или недоступности сервиса оформление заказа невозможно.

## 🐳 Запуск через Docker Compose

Самый быстрый способ развернуть весь комплекс со всеми зависимостями (Redis + Payment Service + Main App):

Сборка и запуск всех сервисов:

```bash
docker compose --profile full up -d
```

Проверка статуса запущенных контейнеров:

```bash
docker compose --profile full ps
```

Просмотр логов:

```bash
docker compose --profile full logs -f
```

Остановка и удаление контейнеров:

```bash
docker compose --profile full down
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