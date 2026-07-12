# Интеграция SonarQube в проект Payment Service

## Контекст проекта
- **Стек:** Java 21, Spring Boot 3.3.0, Maven
- **Репозиторий:** GitHub
- **CI/CD:** GitHub Actions
- **SonarQube:** SonarCloud (облачное решение)
- **Текущее состояние:** Проект собирается и тестируется через CI, SonarQube не подключён

---

## Шаг 1. Подготовка SonarCloud

### 1.1 Регистрация и создание проекта

1. Перейдите на https://sonarcloud.io и войдите через GitHub
2. Создайте организацию:
   - Нажмите "Create new organization"
   - Выберите "GitHub" для авторизации
   - Назовите организацию (например: `your-org`)

3. Создайте проект:
   - Нажмите "Analyze new project"
   - Выберите "GitHub"
   - Авторизуйте доступ к репозиторию
   - Выберите репозиторий `Innowise_Vitali_Payment_Service`
   - Укажите ключ проекта (например: `innowise-vitali-payment-service`)
   - Выберите "Maven" как систему сборки

4. Сохраните данные:
   - **Organization:** `[YOUR_SONAR_ORGANIZATION]`
   - **Project Key:** `[YOUR_SONAR_PROJECT_KEY]`
   - **Sonar Token:** `[YOUR_SONAR_TOKEN]` (получите в Settings > My Account > Security)

### 1.2 Настройка GitHub Secrets

В репозитории GitHub добавьте следующие secrets:
- `SONAR_TOKEN` - токен из SonarCloud
- `SONAR_ORGANIZATION` - ваша организация в SonarCloud
- `SONAR_PROJECT_KEY` - ключ проекта

**Путь:** Settings > Secrets and variables > Actions > New repository secret

---

## Шаг 2. Настройка pom.xml

Добавьте плагины и свойства для SonarQube и Jacoco:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>Innowise_Vitali_Payment_Service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Innowise_Vitali_Payment_Service</name>
    <description>Innowise_Vitali_Payment_Service</description>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <lombok.version>1.18.32</lombok.version>
        <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
        <wiremock.version>3.5.4</wiremock.version>
        
        <!-- SonarQube Properties -->
        <sonar.projectKey>[YOUR_SONAR_PROJECT_KEY]</sonar.projectKey>
        <sonar.organization>[YOUR_SONAR_ORGANIZATION]</sonar.organization>
        <sonar.host.url>https://sonarcloud.io</sonar.host.url>
        <sonar.coverage.jacoco.xmlReportPaths>
            ${project.build.directory}/site/jacoco/jacoco.xml
        </sonar.coverage.jacoco.xmlReportPaths>
        <sonar.qualitygate.wait>true</sonar.qualitygate.wait>
        <sonar.java.source>21</sonar.java.source>
        <sonar.java.binaries>${project.build.directory}/classes</sonar.java.binaries>
        <sonar.java.test.binaries>${project.build.directory}/test-classes</sonar.java.test.binaries>
        <sonar.sources>src/main/java</sonar.sources>
        <sonar.tests>src/test/java</sonar.tests>
    </properties>

    <dependencies>
        <!-- ... существующие зависимости ... -->
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <!-- JaCoCo Maven Plugin for Code Coverage -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>check</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>PACKAGE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.80</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            
            <!-- SonarQube Maven Plugin -->
            <plugin>
                <groupId>org.sonarsource.scanner.maven</groupId>
                <artifactId>sonar-maven-plugin</artifactId>
                <version>3.10.0.2594</version>
            </plugin>
            
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>${lombok-mapstruct-binding.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            
            <!-- Maven Checkstyle Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-checkstyle-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <configLocation>checkstyle.xml</configLocation>
                    <consoleOutput>true</consoleOutput>
                    <failsOnError>true</failsOnError>
                    <linkXRef>false</linkXRef>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Шаг 3. Настройка CI-пайплайна (GitHub Actions)

Обновите `.github/workflows/ci.yml`:

```yaml
name: CI Pipeline

on:
  push:
    branches: [ "main", "develop", "feature/**" ]
  pull_request:
    branches: [ "main", "develop" ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Important for SonarQube analysis

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Grant execute permission for mvnw
        run: chmod +x mvnw

      - name: Build
        run: ./mvnw clean package -DskipTests

      - name: Test with coverage
        run: ./mvnw test jacoco:report

      - name: Code Analysis
        run: ./mvnw checkstyle:check
        continue-on-error: true

      - name: SonarQube Scan
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_ORGANIZATION: ${{ secrets.SONAR_ORGANIZATION }}
          SONAR_PROJECT_KEY: ${{ secrets.SONAR_PROJECT_KEY }}
        run: ./mvnw sonar:sonar \
          -Dsonar.projectKey=${{ secrets.SONAR_PROJECT_KEY }} \
          -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION }}

      - name: SonarQube Quality Gate check
        uses: SonarSource/sonarqube-quality-gate-action@master
        timeout-minutes: 5
        with:
          scanMetadataReportFile: target/sonar/report-task.txt
```

---

## Шаг 4. Настройка Quality Gate

### 4.1 Рекомендуемые пороги Quality Gate

В SonarCloud перейдите в Quality Gates и создайте/отредактируйте Quality Gate:

| Метрика | Оператор | Значение | Обоснование |
|---------|----------|----------|-------------|
| Coverage on New Code | ≥ | 80% | Стандарт индустрии для критического кода |
| Critical Issues on New Code | = | 0 | Критические баги недопустимы |
| Major Issues on New Code | ≤ | 5 | Допускаются минимальные ошибки |
| Code Smells on New Code | ≤ | 10 | Допускаются минимальные code smells |
| Security Hotspots on New Code | ≤ | 5 | Допускаются минимальные риски |
| Duplicated Lines on New Code | ≤ | 3% | Минимальное дублирование |
| Technical Debt Ratio on New Code | ≤ | 5% | Поддерживаемый уровень техдолга |
| Reliability Rating on New Code | = | A | Надёжность на уровне A |
| Security Rating on New Code | = | A | Безопасность на уровне A |
| Maintainability Rating on New Code | ≥ | B | Поддерживаемость минимум B |

### 4.2 Настройка Quality Profile

1. Перейдите в Quality Profiles
2. Создайте новый профиль на основе "Sonar way"
3. Активируйте дополнительные правила:
   - Cognitive Complexity (порог: 15)
   - Function complexity (порог: 10)
   - Magic numbers
   - Unused imports
   - String literals should not be duplicated

---

## Шаг 5. Проверка результата

### Чеклист проверки интеграции

- [ ] **SonarCloud настроен:**
  - [ ] Организация создана
  - [ ] Проект создан с правильным key
  - [ ] Токен получен и добавлен в GitHub Secrets
  - [ ] Organization и Project Key добавлены в GitHub Secrets

- [ ] **pom.xml обновлён:**
  - [ ] Свойства sonar.* добавлены с плейсхолдерами
  - [ ] jacoco-maven-plugin добавлен
  - [ ] sonar-maven-plugin добавлен
  - [ ] Плейсхолдеры заменены на реальные значения

- [ ] **GitHub Actions обновлён:**
  - [ ] Шаг SonarQube Scan добавлен
  - [ ] Шаг Quality Gate check добавлен
  - [ ] Secrets настроены в репозитории
  - [ ] fetch-depth: 0 добавлен в checkout

- [ ] **Quality Gate настроен:**
  - [ ] Пороги установлены согласно рекомендациям
  - [ ] Quality Gate назначен проекту

- [ ] **Тестовый запуск:**
  - [ ] Commit и push в ветку
  - [ ] CI pipeline запустился успешно
  - [ ] SonarQube анализ прошёл
  - [ ] Отчёт доступен в SonarCloud
  - [ ] Quality Gate прошёл (или понятны причины провала)

### Где смотреть отчёт

1. **GitHub Actions:**
   - Перейдите в Actions tab репозитория
   - Откройте последний запуск workflow
   - Проверьте шаг "SonarQube Scan" и "SonarQube Quality Gate check"

2. **SonarCloud:**
   - Перейдите на https://sonarcloud.io
   - Откройте ваш проект
   - Смотрите:
     - **Overview** - общая оценка (A/B/C/D)
     - **Code** - детальный анализ кода
     - **Issues** - найденные проблемы
     - **Measures** - метрики покрытия, дублирования и т.д.

### Как читать статус Quality Gate

- **PASSED** - Все критерии выполнены, код можно мержить
- **FAILED** - Есть критические проблемы:
  - Проверьте раздел "Issues" в SonarCloud
  - Исправьте Critical/Major issues
  - Улучшите покрытие кода тестами
  - Уменьшите дублирование
- **WARNING** - Есть некритичные проблемы, но мерж возможен
- **NONE** - Quality Gate не настроен

### Что делать при провале Quality Gate

1. **Низкое покрытие:**
   - Добавьте unit тесты для непокрытого кода
   - Убедитесь, что тесты запускаются в CI

2. **Code smells:**
   - Рефакторинг длинных методов
   - Удаление дублирования
   - Упрощение сложных условий

3. **Security issues:**
   - Исправьте уязвимости безопасности
   - Удалите hardcoded credentials
   - Используйте безопасные библиотеки

4. **Duplications:**
   - Вынесите общий код в утилитные классы
   - Используйте наследование/композицию

---

## Итоговый чеклист "Готово / Не готово"

| Этап | Статус | Комментарий |
|------|--------|-------------|
| SonarCloud организация создана | ☐ | |
| SonarCloud проект создан | ☐ | |
| GitHub Secrets добавлены | ☐ | SONAR_TOKEN, SONAR_ORGANIZATION, SONAR_PROJECT_KEY |
| pom.xml обновлён | ☐ | Плагины и свойства добавлены |
| GitHub Actions обновлён | ☐ | Шаги SonarQube добавлены |
| Quality Gate настроен | ☐ | Пороги установлены |
| Тестовый запуск успешен | ☐ | CI pipeline прошёл |
| Отчёт доступен в SonarCloud | ☐ | Можно просмотреть анализ |

---

## Дополнительные команды для локального тестирования

### Локальный запуск SonarQube анализа
```bash
# Установите переменные окружения
export SONAR_TOKEN=your-token
export SONAR_ORGANIZATION=your-org
export SONAR_PROJECT_KEY=your-project-key

# Запустите анализ
./mvnw clean test jacoco:report sonar:sonar
```

### Проверка покрытия локально
```bash
./mvnw clean test jacoco:report
# Отчёт будет в target/site/jacoco/index.html
```

### Проверка SonarQube свойств
```bash
./mvnw help:evaluate -Dexpression=sonar.projectKey -q -DforceStdout
```
