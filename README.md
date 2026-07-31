# my-market-back-app

Бэкенд приложения «Витрина интернет-магазина»

---

## 🚀 О проекте

Веб-приложение представляет собой полноценную интерактивную витрину интернет-магазина с возможностью поиска, пагинации, фильтрации, управления корзиной покупателя и эмуляцией оформления заказов. Разработано на платформе **Java 21** с использованием **Spring Boot** на классическом блокирующем стеке технологий.

Интерфейс приложения построен на серверном рендеринге шаблонов **Thymeleaf**, взаимодействующих с контроллерами Spring Web MVC.

---

## 🛠 Технологический стек

- **Язык:** Java 21
- **Фреймворк:** Spring Boot Framework 4+ (Starter Web, Starter Data JPA, Thymeleaf)
- **Сборка:** Maven
- **База данных:** H2 Database (In-Memory персистентное хранилище с доступом к консоли)
- **ORM:** Hibernate / Spring Data JPA
- **Деплой:** Executable JAR (Многоэтапная Docker-контейнеризация, порт 8080)
- **Тестирование:** JUnit 5, Spring Boot Test Framework, Mockito

---

## 📂 Структура проекта

```bash
my-market-back-app/
├── src/
│   ├── main/
│   │   ├── java/ru/yandex/practicum/mymarket/
│   │   │   ├── configurations/       # Первоначальное наполнение БД демонстрационными товарами
│   │   │   ├── controllers/          # Web-контроллеры страниц (Catalog, Cart, Order)
│   │   │   ├── helpers/              # Вспомогательные утилиты и хелперы
│   │   │   ├── interfaces/           # Интерфейсы сервисов бизнес-логики (ItemService, CartService, etc.)
│   │   │   ├── mappers/              # Преобразование сущностей и моделей данных
│   │   │   ├── models/               # Сущности БД (Entity), доменные модели и Enums
│   │   │   ├── repositories/         # Интерфейсы Spring Data JPA репозиториев
│   │   │   ├── services/             # Сервисный слой с реализацией бизнес-логики
│   │   │   ├── viewmodels/           # UI модели представления (Immutable Java Records)
│   │   │   └── MyMarketAppApplication.java
│   │   └── resources/
│   │       ├── templates/            # HTML-шаблоны страниц Thymeleaf (items, item, cart, orders, order)
│   │       ├── static/images/        # Статические ресурсы (изображения товаров)
│   │       └── application.properties
│   └── test/java/...                 # Юнит- и интеграционные тесты веб-слоя (MockMvc)
├── pom.xml
├── Dockerfile
└── .gitignore
```

---

## ⚙️ Конфигурация и запуск

### Переменные окружения (src/main/resources/application.properties)

Приложение настроено на работу с легковесной базой данных H2 в оперативной памяти:

```bash
spring.application.name=my-market-app

spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:my-market-app
spring.datasource.username=
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

spring.h2.console.enabled=true
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

## 📋 Основные возможности API

Каталог товаров (CatalogController)

| Метод    | Эндпоинт                            | Описание              |
|----------|-------------------------------------|-----------------------|
| `GET`    | `/ или /items`                      | Постраничный поиск товаров |
| `GET`    | `/items/{id}`                       | Получение поста       |
| `POST`   | `/items`                            | Изменение количества товара в корзине |
| `...`     |                                     |                       |

Корзина покупателя (CartController)

| Метод  | Эндпоинт      | Описание                              |
|--------|---------------|---------------------------------------|
| `GET`  | `/cart/items` | Страница корзины со списком товаров   |
| `POST` | `/cart/items` | Изменение составка корзины            |
| `...`  |               |                                       |

Управление заказами (OrderController)

| Метод    | Эндпоинт       | Описание                     |
|----------|----------------|------------------------------|
| `GET`    | `/orders`      | Страница истории заказов     |
| `GET`    | `/orders/{id}` | Страница совершенного заказа |
| `POST`   | `/buy`         | Сервис проведения покупки    |
| `...`     |                |                              |


---

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