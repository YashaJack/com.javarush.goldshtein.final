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

Сразу напишу: у меня так и не получилось запустить проект, так что все задания выполнял "в слепую".
1. Разобрался со структурой проекта (не считается, но я старался)).
2. Удалил социальные сети (VK, Yandex):
    - удалены классы `YandexOAuth2UserDataHandler`, `VkOAuth2UserDataHandler`
    - удалены все упоминания провайдеров `yandex` и `vk` из конфигурации и кода
3. Вынесены секреты в переменные окружения::
    - БД: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
    - OAuth: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
    - Почта: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
    - В `application.yml` все чувствительные поля читаются через `${ENV_VAR}`
    - Добавлен шаблон `.env.example`, `.env` добавлен в `.gitignore`
   
5. Создан тестовый класс ProfileRestControllerTest
      -	Используется MockMvc с standaloneSetup(...)
      -	Зависимости ProfileRepository и ProfileMapper подменены на фейковые реализации.
      -	Добавлен HandlerMethodArgumentResolver, чтобы в тестах корректно подставлялся @AuthenticationPrincipal AuthUser.
   
    - Реализованы два теста:
      -	get_returnsProfileForAuthenticatedUser — проверяет возврат профиля по текущему пользователю
      -	update_updatesProfileForAuthenticatedUser — проверяет успешное обновление профиля
      
6. Рефакторинг FileUtil#upload:
    - перевёл на Java NIO (Path, Files)
    - Добавил авто-создаваемую директорию, нормализацию путей, защиту от path traversal и запись через Files.copy(..., REPLACE_EXISTING)
    - Методы download и delete также переписаны на NIO

9. Dockerfile для основного сервера:
   - Добавлен двухэтапный `Dockerfile`:
     - Stage 1 (maven+JDK17) собирает проект с кэшированием зависимостей 
     - Stage 2 (JDK17) содержит только собранный `app.jar`
     
   - Запуск:
     - ```bash
       docker build -t jira-app .
       docker run --rm -p 8080:8080 jira-app