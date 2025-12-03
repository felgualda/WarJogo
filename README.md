# ⚔️ WarJogo - Grupo 7 (Engenharia de Software II)

Um projeto de implementação do jogo **War** utilizando **Java** e o framework **libGDX**. Este projeto foi gerado com [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

Documentação referente ao jogo, como requisitos e análise de riscos: https://drive.google.com/file/d/1-0hCkskSjQH_A5zjcHE9YAWiejNya6Jr/view?usp=sharing

---

## 🚀 Como Executar (Entrega Final)

O jogo foi compilado para rodar nativamente nos três principais sistemas operacionais (sem necessidade de instalação prévia do Java), além de uma versão universal.

### 📂 Estrutura da Entrega

A entrega está organizada na pasta `Jogo_War_Grupo7` da seguinte forma:

* **`/` (Raiz):** Contém o arquivo `WarJogo.jar` (Versão Universal Leve).
* **`/Windows`:** Contém a versão executável (`.exe`) com Java embutido para Windows.
* **`/Linux`:** Contém o executável nativo para distribuições Linux.
* **`/MAC`:** Contém os aplicativos (`.app`) para macOS (Intel e Apple Silicon).

---

### Instruções de Execução por Sistema Operacional

#### 1. Windows (Recomendado) - https://drive.google.com/drive/folders/14SOZJVoIDb_kca6wTPZUo-VBV8_0vEO3?usp=sharing

Esta versão já inclui o Java necessário embutido na pasta.

1.  Abra a pasta **`Windows`**.
2.  Localize o arquivo **`WarJogo.exe`** (pode aparecer apenas como `WarJogo`).
3.  Dê um **clique duplo** para iniciar.

#### 2. Linux - https://drive.google.com/drive/folders/12R2fg5o_6ycnnVwxyXl1VrKrq0JLCg5g?usp=drive_link

1.  Abra a pasta **`Linux`**.
2.  Localize o arquivo executável (geralmente sem extensão ou `.x86_64`).
3.  Dê um **clique duplo** ou execute via terminal:

    ```bash
    ./WarJogo
    ```
    > **Nota:** Se o arquivo não abrir, garanta que ele tem **permissão de execução**: *Botão direito -> Propriedades -> Permissões -> "Permitir executar como programa"*.

#### 3. macOS - https://drive.google.com/drive/folders/1CiwerGcQjIO7iqXyqbJRXJnh_DfrwYiF?usp=sharing

Na pasta `MAC`, você encontrará duas subpastas. Escolha a correta para o seu processador:

* **`macX64`**: Para Macs com processador **Intel** (modelos mais antigos).
* **`macM1` (ou `macArm64`)**: Para Macs com processador **Apple Silicon** (M1, M2, M3...).

> **Nota sobre Segurança (Gatekeeper):** O macOS pode bloquear a abertura inicial. Para contornar:
>
> 1.  Clique com o **Botão Direito** (ou `Control` + Clique) no ícone do aplicativo `WarJogo.app`.
> 2.  Selecione **"Abrir"** no menu.
> 3.  Na janela de aviso, confirme clicando em **"Abrir"**.

#### 4. Versão Universal (`.JAR`) - https://drive.google.com/file/d/1jv84HC_b7lKWARywIcNrwe1hg2Dl4d6g/view?usp=sharing

Requer **Java 17+** instalado.

1.  Na pasta raiz, localize o arquivo **`WarJogo.jar`**.
2.  Dê um **clique duplo** ou abra via terminal:

    ```bash
    java -jar WarJogo.jar
    ```

---

## 🛠️ Documentação Técnica (Desenvolvimento)

### Plataformas do Projeto

* **`core`**: Módulo principal com a **lógica da aplicação** compartilhada por todas as plataformas.
* **`lwjgl3`**: Plataforma desktop primária usando LWJGL3.

### Gradle & Comandos Úteis

Este projeto usa **Gradle** para gerenciar dependências. O *wrapper* do Gradle foi incluído.

| Comando | Descrição |
| :--- | :--- |
| `build` | Compila fontes e gera arquivos de todos os projetos. |
| `clean` | Remove pastas `build` (limpeza). |
| `lwjgl3:jar` | Gera o JAR executável em `lwjgl3/build/libs`. |
| `lwjgl3:run` | Inicia a aplicação em **modo de desenvolvimento**. |
| `test` | Roda os testes unitários. |

### Testes Unitários

Este projeto inclui testes unitários automatizados com **JUnit 5** e **AssertJ** para validar as regras de negócio.

#### Como Executar os Testes

```bash
# Rodar todos os testes do módulo core
./gradlew :core:test

# Ou no Windows
.\gradlew.bat :core:test

### Cobertura de Testes

Os testes cobrem as principais lógicas do jogo, localizados em `core/src/test/java/com/gruposete/war/core/`:

* **`AtaqueLogicaTest.java`**: Valida regras de combate (ex: mín. 2 tropas para atacar) e conquista de territórios.
* **`IABotTest.java`**: Valida as heurísticas de inteligência artificial (eficácia de ataque e refinamento de defesa).
* **`JogadorTest.java`**: Testes de gerenciamento de saldo de exércitos e posse.
* **`CartaTest.java`**: Testes de estrutura de cartas.
* **`VerificadorObjetivosTest.java`**: Valida condições de vitória (territórios, continentes, destruição).
* **`ControladorDePartidaTest.java`**: Testes de fluxo de turno, alocação e troca de cartas.

#### Status Atual
✅ Todos os testes passando.

---

## 🔁 CI/CD com GitHub Actions

Este projeto utiliza **GitHub Actions** para garantir a qualidade do código a cada alteração. O *workflow* (`.github/workflows/build-and-test.yml`) executa automaticamente:

* Setup do ambiente Java (JDK 17 e 21).
* Compilação do projeto.
* Execução de todos os testes unitários.

A validação ocorre em **3 sistemas operacionais simultaneamente**:

* `Windows-latest`
* `macOS-latest`
* `Ubuntu-latest`

### Relatório de Testes (HTML)

Após rodar os testes localmente, um relatório detalhado pode ser encontrado em:

`core/build/reports/tests/test/index.html`
