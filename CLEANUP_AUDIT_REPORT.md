# Аудит и чистка проекта - Отчёт

**Проект:** Innowise_Vitali_Payment_Service  
**Стек:** Java 21, Spring Boot 3.3.0, Maven, MongoDB, Kafka  
**Дата аудита:** 10.07.2026

---

## Таблица элементов для удаления

| Файл/Класс/Метод | Тип | Причина удаления | Уверенность |
|------------------|-----|------------------|------------|
| `patch.json` | Файл | Временный файл с Redis паролем, не используется в проекте | Высокая |
| `sp.yaml` | Файл | Kubernetes Pod dump (storage-provisioner), не относится к проекту | Высокая |
| `k8s/kafka-deployment.yaml` | Файл | Kafka должна браться из общего инфра-оркестратора, не дублируется | Высокая |
| `docker-compose-sonarqube.yml` | Файл | Пример для локального SonarQube, не используется в основном проекте | Средняя |
| `.github/workflows/sonar.yml` | Файл | Пример workflow для SonarQube, не настроен (нет secrets) | Средняя |
| `example-controller/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `example-dto/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `example-exception/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `example-model/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `example-repository/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `example-service/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `example-test/` | Папка | Примеры кода для документации, не используются в основном проекте | Высокая |
| `EXAMPLE_PROJECT_STRUCTURE.md` | Файл | Документация-пример, не относится к рабочему проекту | Высокая |
| `EXAMPLE_POM_XML.md` | Файл | Документация-пример, не относится к рабочему проекту | Высокая |
| `JAVA_BEST_PRACTICES_CHECKLIST.md` | Файл | Документация-пример, не относится к рабочему проекту | Средняя |
| `QUALITY_GATE_CONFIG.md` | Файл | Документация-пример, не относится к рабочему проекту | Средняя |
| `HELP.md` | Файл | Стандартный Spring Boot файл-заглушка, не содержит полезной информации | Низкая |
| `src/main/resources/db/changelog/` | Папка | Liquibase отключён в application.yml (enabled: false), changelog не используется | Средняя |
| `LiquibaseConfig.java` | Класс | Liquibase отключён, конфигурация не выполняется | Средняя |
| `liquibase-core` зависимость | pom.xml | Liquibase отключён и не используется в runtime | Средняя |
| `liquibase-mongodb` зависимость | pom.xml | Liquibase отключён и не используется в runtime | Средняя |

---

## Элементы, которые НЕ удалять

| Элемент | Причина сохранения |
|---------|-------------------|
| `spring-boot-starter-security` | Используется в SecurityConfig.java и InternalSecretFilter.java |
| `mapstruct` | Используется в PaymentMapper.java (вызывается из PaymentService) |
| `wiremock-standalone` | Используется в PaymentIntegrationTest.java для мокинга external API |
| `testcontainers` (mongodb, kafka, junit-jupiter) | Используются в PaymentIntegrationTest.java |
| `spring-kafka` | Используется в KafkaProducerConfig, KafkaConsumerConfig, PaymentProducer |
| `spring-boot-starter-validation` | Используется в CreatePaymentRequest (@NotNull, @NotBlank) |
| `PaymentRepositoryCustom` + `PaymentRepositoryCustomImpl` | Используются для кастомных запросов (агрегации для суммы) |
| `.github/workflows/ci.yml` | Основной CI pipeline для сборки и тестов |
| `checkstyle.xml` | Используется в maven-checkstyle-plugin в pom.xml |

---

## Изменения, которые можно применить автоматически

### 1. Удалить временные и посторонние файлы (высокая уверенность)
```bash
rm patch.json
rm sp.yaml
rm k8s/kafka-deployment.yaml
rm -rf example-controller/
rm -rf example-dto/
rm -rf example-exception/
rm -rf example-model/
rm -rf example-repository/
rm -rf example-service/
rm -rf example-test/
rm EXAMPLE_PROJECT_STRUCTURE.md
rm EXAMPLE_POM_XML.md
```

### 2. Удалить Liquibase (средняя уверенность)
```bash
rm -rf src/main/resources/db/
rm src/main/java/com/example/innowise_vitali_payment_service/config/LiquibaseConfig.java
```
**Из pom.xml удалить:**
```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
<dependency>
    <groupId>org.liquibase.ext</groupId>
    <artifactId>liquibase-mongodb</artifactId>
    <version>${liquibase-mongodb.version}</version>
</dependency>
```
**Из properties удалить:**
```xml
<liquibase-mongodb.version>4.28.0</liquibase-mongodb.version>
```
**Из application.yml удалить:**
```yaml
spring:
  liquibase:
    enabled: false
```

### 3. Удалить документацию-примеры (средняя уверенность)
```bash
rm JAVA_BEST_PRACTICES_CHECKLIST.md
rm QUALITY_GATE_CONFIG.md
rm docker-compose-sonarqube.yml
rm .github/workflows/sonar.yml
```

---

## Элементы, требующие ручной проверки перед удалением

### 1. `HELP.md`
- **Причина:** Стандартный Spring Boot файл
- **Рекомендация:** Проверить, нужна ли документация для новых разработчиков. Если нет - удалить.

### 2. Liquibase (если планируется включение в будущем)
- **Причина:** Отключён сейчас, но changelog существует
- **Рекомендация:** Если миграции БД не нужны - удалить полностью. Если планируется включение - оставить.

### 3. SonarQube workflow и docker-compose
- **Причина:** Могут быть полезны для локальной разработки
- **Рекомендация:** Если команда использует SonarQube - настроить и оставить. Если нет - удалить.

---

## Итоговая рекомендация

### Приоритет 1 (удалить немедленно):
1. `patch.json` - временный файл
2. `sp.yaml` - Kubernetes dump
3. `k8s/kafka-deployment.yaml` - противоречит архитектуре (Kafka из оркестратора)
4. Все `example-*` папки и `EXAMPLE_*.md` файлы - примеры кода для документации

### Приоритет 2 (удалить после проверки):
1. Liquibase полностью (если миграции не нужны)
2. SonarQube связанные файлы (если не используются)
3. `HELP.md` (если документация не нужна)

### Приоритет 3 (оставить):
1. Все зависимости из pom.xml - все используются
2. `.github/workflows/ci.yml` - основной CI pipeline
3. `checkstyle.xml` - используется для code quality

---

## Статистика

- **Всего найдено лишних элементов:** 18
- **Высокая уверенность в удалении:** 11
- **Средняя уверенность:** 7
- **Низкая уверенность:** 1 (HELP.md)
- **Зависимости для удаления:** 2 (liquibase-core, liquibase-mongodb)
- **Классы для удаления:** 1 (LiquibaseConfig)
