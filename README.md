# 🧩 Auth Service (Spring Boot)

## 📌 Описание проекта
**Auth Service** — это сервис аутентификации и авторизации, предназначенный для управления
доступом пользователей в микросервисной системе.

Сервис:
- выполняет регистрацию и логин пользователей
- поддерживает JWT-аутентификацию
- поддерживает OAuth2 / OIDC вход
- интегрирован с RabbitMQ для событий
- использует Spring Security
- применяет AOP для логирования
- готов к деплою в Docker и Kubernetes

Проект является частью **микросервисной архитектуры**.

---

## ⚙️ Стек технологий
- Java 17+
- Spring Boot
- Spring Security
- JWT (jjwt)
- OAuth2 / OIDC
- Spring Data JPA
- Hibernate
- RabbitMQ (AMQP)
- Lombok
- MapStruct
- AOP (AspectJ)
- MySQL
- Docker
- Kubernetes
- Minikube

---

## 🔐 Аутентификация и безопасность

### JWT
- JWT создаётся с использованием алгоритма **HS512**
- Хранится:
  - 🍪 в **HttpOnly cookie**
  - 📩 в `Authorization: Bearer`
- Содержит:
  - email пользователя
  - роли (`ROLE_USER`, `ROLE_ADMIN`)

---

### JwtFilter
- Извлекает JWT:
  - из `Authorization` header
  - или из cookie `jwt`
- Валидирует токен
- Устанавливает `SecurityContext`

---

### SecurityConfig
- Stateless-сессии (`STATELESS`)
- CSRF отключён
- Разрешён доступ:
  - `/api/auth/**`
  - `/auth/**`
  - `/oauth2/**`
- Остальные эндпоинты требуют аутентификацию

---

## 🌐 OAuth2 / OIDC

Поддержка входа через OAuth2 / OIDC провайдеров:

- автоматическое создание пользователя
- обновление данных при повторном входе
- генерация JWT после успешного OAuth2 login
- сохранение пользователя в БД

Используются:
- `CustomOAuth2UserService`
- `CustomOidcUser`

---

## RabbitMQ

### Exchanges
- `auth.exchange`
- `log.exchange`
- `home.exchange`
- `booking.exchange`

### Queues
- `auth.travel.agency.queue`
- `log.travel.agency.queue`
- `home.travel.agency.queue`
- `booking.travel.agency.queue`

### Routing keys
- `auth.#`
- `log.#`
- `home.#`
- `booking.#`

### Назначение
- 📢 событие регистрации пользователя
- 📢 событие успешного логина
- 📜 отправка логов в Log Service
- 🔄 асинхронное взаимодействие между сервисами

---

## 📊 Логирование (AOP)

### LoggingAspect
- логирует вход и выход из методов
- выводит параметры и результат
- измеряет время выполнения

### DetailedLoggingAspect
- поддерживает вложенные вызовы
- визуализирует лог-блоки
- предупреждает о медленных методах
- маскирует чувствительные данные:
  - password
  - token
  - secret

---
