# 🧩 Auth Service (Spring Boot)

## 📌 Описание проекта
**Auth Service** — это сервис аутентификации и авторизации, реализованный на **Spring Boot**,  
который поддерживает:

- 🔐 JWT-аутентификацию
- 🌐 OAuth2 (OIDC) вход
- 🐰 RabbitMQ для межсервисных событий
- 📊 AOP-логирование
- 🗄️ JPA / Hibernate
- 🔑 Spring Security

Проект предназначен для использования в **микросервисной архитектуре**.

---

## ⚙️ Стек технологий
- Java 17+
- Spring Boot
- Spring Security
- JWT (jjwt)
- OAuth2 / OIDC
- Spring Data JPA
- RabbitMQ (AMQP)
- Hibernate
- Lombok
- MapStruct
- AOP (AspectJ)
- MySQL 

---



## Аутентификация и безопасность

### JWT
- JWT создаётся с использованием **HS512**
- Хранится:
    - 🍪 в **HttpOnly cookie**
    - 📩 в **Authorization Header**
- Содержит:
    - email
    - роли (`ROLE_USER`, и т.д.)

### JwtFilter
- Проверяет JWT:
    - из `Authorization: Bearer`
    - или из cookie `jwt`
- Устанавливает `SecurityContext`

### SecurityConfig
- `STATELESS` сессии
- Отключён CSRF
- Разрешён доступ:
    - `/api/auth/**`
    - `/auth/**`
    - `/oauth2/**`
- Остальные запросы требуют аутентификацию

---

## OAuth2 / OIDC
Поддержка входа через OAuth2 провайдера:

- Автоматическое создание пользователя
- Обновление данных при повторном входе
- Генерация JWT после успешного OAuth2 login
- Сохранение пользователя в БД

Используется:
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
- 📢 события регистрации
- 📢 события логина
- 📜 централизованное логирование