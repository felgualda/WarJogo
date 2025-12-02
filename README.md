# WarJogo

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws a simple GUI on the screen.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Testes Unitários

Este projeto inclui testes unitários com **JUnit 5** e **AssertJ**.

### Executar Testes

```bash
# Rodar todos os testes do módulo core
./gradlew :core:test

# Ou no Windows
.\gradlew.bat :core:test

# Rodar os testes com output formatado (test-logger)
./gradlew :core:test
```

### Estrutura de Testes

- `core/src/test/java/com/gruposete/war/core/JogadorTest.java` - Testes de gerenciamento de jogadores
- `core/src/test/java/com/gruposete/war/core/CartaTest.java` - Testes de cartas
- `core/src/test/java/com/gruposete/war/core/VerificadorObjetivosTest.java` - Testes de verificação de objetivos
- `core/src/test/java/com/gruposete/war/core/ControladorDePartidaTest.java` - Testes do controlador de partida

### Status dos Testes

✅ **12/12 Testes Passando**
- 3 testes em JogadorTest
- 1 teste em CartaTest
- 4 testes em VerificadorObjetivosTest
- 4 testes em ControladorDePartidaTest

### CI/CD com GitHub Actions

Este projeto utiliza **GitHub Actions** para executar testes automaticamente em:
- Windows
- macOS
- Ubuntu

Com as versões de Java: 17 e 21

O workflow está configurado em `.github/workflows/build-and-test.yml` e roda a cada push.

### Relatório de Testes

Após rodar os testes, você pode visualizar um relatório HTML:
```bash
./gradlew :core:test
```

O relatório estará em: `core/build/reports/tests/test/index.html`

