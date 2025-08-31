## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

```
  url: jdbc:postgresql://localhost:5432/jira
  username: jira
  password: JiraRush
```

- Есть 22 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем
      проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

Список выполненных задач:

⚠️ Важно: проект у меня так и не удалось собрать и запустить, поэтому все задания выполнялись "вслепую", основываясь на коде и структуре проекта.

1. Разобрался со структурой проекта (не считается, но я старался 🙂).

2. Удалил социальные сети (VK, Yandex):
    - удалены классы `YandexOAuth2UserDataHandler`, `VkOAuth2UserDataHandler`
    - убраны все упоминания провайдеров `yandex` и `vk` из конфигурации и кода.

3. Вынесены секреты в переменные окружения:
    - БД: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
    - OAuth: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GITLAB_CLIENT_ID`, `GITLAB_CLIENT_SECRET`
    - Почта: `MAIL_USERNAME`, `MAIL_PASSWORD`
    - В `application.yml` все чувствительные поля читаются через `${ENV_VAR}`
    - Добавлен шаблон `.env.example`, `.env` внесён в `.gitignore`.

5. Создан тестовый класс `ProfileRestControllerTest`:
    - используется `MockMvc` с `standaloneSetup(...)`
    - зависимости `ProfileRepository` и `ProfileMapper` подменены на фейковые реализации
    - добавлен `HandlerMethodArgumentResolver`, чтобы корректно подставлялся `@AuthenticationPrincipal AuthUser`
    - реализованы два теста:
        - `get_returnsProfileForAuthenticatedUser` — проверяет возврат профиля текущего пользователя
        - `update_updatesProfileForAuthenticatedUser` — проверяет обновление профиля.

6. Рефакторинг `FileUtil#upload`:
    - переведён на Java NIO (`Path`, `Files`)
    - добавлено авто-создание директорий, нормализация путей, защита от path traversal
    - запись реализована через `Files.copy(..., REPLACE_EXISTING)`
    - методы `download` и `delete` также переписаны на NIO.

9. Dockerfile для приложения:
    - добавлен двухэтапный `Dockerfile`:
        - **Stage 1** (Maven + JDK17) — сборка проекта с кэшированием зависимостей
        - **Stage 2** (JDK17) — содержит только собранный `app.jar`
    - запуск:
      ```bash
      docker build -t jira-app .
      docker run --rm -p 8080:8080 jira-app
      ```

11. Поддержка мультиязычности (i18n):
- созданы файлы переводов:
    - `src/main/resources/messages.properties` (русский — по умолчанию)
    - `src/main/resources/messages_en.properties` (английский)
- в шаблонах Thymeleaf текст заменён на ключи, например:
    - `Logout` → `#{logout}`
    - `Login` → `#{login}`
- теперь интерфейс приложения поддерживает переключение локалей.es_en.properties` (английский)