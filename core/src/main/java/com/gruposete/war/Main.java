package com.gruposete.war;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.gruposete.war.core.*;
import com.gruposete.war.ui.*;

// Imports adicionados para a nova lógica de setup
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Main extends ApplicationAdapter {
    private TelaInicial telaInicial;
    private TelaDeSelecaoDeJogadores telaDeSelecao;
    private TelaDeJogo telaDeJogo; // A telaDeJogo agora começa null
    private TelaDeRegras telaDeRegras;
    private TelaDeConfig telaDeConfig;
    private TelaVitoria telaVitoria;


    private enum TelaAtiva { INICIAL, SELECAO, JOGO, REGRAS, CONFIG, VITORIA }
    private TelaAtiva telaAtual;

    @Override
    public void create() {
        // A telaDeJogo não é mais criada aqui.
        // Ela será criada sob demanda (quando o jogo iniciar)
        // pois depende dos dados do setup.

        // --- Lógica de setup movida para os callbacks da TelaDeSelecao ---

        // 1. Criamos os callbacks para a nova TelaDeSelecao.
        // O 'iniciarJogoCallback' contém a lógica de setup que antes estava no 'jogarCallback' da TelaInicial.
        Runnable iniciarJogoCallback = () -> {
            // Main.java -> iniciarJogoCallback (CORRIGIDO)
// 1. Pega os jogadores
            List<Jogador> jogadores = telaDeSelecao.getJogadoresSelecionados();

// 2. Cria o Controlador (Tarefa #26)
            ControladorDePartida controlador = new ControladorDePartida(jogadores);

// 3. Manda o controlador se preparar (Tarefas #22, #23, #24)
            controlador.iniciarPartida();

// 4. Cria o callback de voltar (sem mudança)
            Runnable voltarCallback = () -> {
                telaAtual = TelaAtiva.INICIAL;
                Gdx.input.setInputProcessor(telaInicial.stage);
                if (telaDeJogo != null) {
                    telaDeJogo.dispose();
                    telaDeJogo = null;
                }
            };
            Runnable voltarAoMenu = () -> {
                telaAtual = TelaAtiva.INICIAL;
                Gdx.input.setInputProcessor(telaInicial.stage);

                // Limpa telas pesadas
                if (telaDeJogo != null) { telaDeJogo.dispose(); telaDeJogo = null; }
                if (telaVitoria != null) { telaVitoria.dispose(); telaVitoria = null; }
            };

            Consumer<Jogador> onVitoriaCallback = (vencedor) -> {
                // 1. Descarta o jogo atual
                if (telaDeJogo != null) {
                    telaDeJogo.dispose();
                    telaDeJogo = null;
                }

                // 2. Cria a tela de vitória com o vencedor recebido
                telaVitoria = new TelaVitoria(voltarAoMenu, vencedor);

                // 3. Muda o estado
                telaAtual = TelaAtiva.VITORIA;
                Gdx.input.setInputProcessor(telaVitoria.stage);
            };

// 5. Cria a TelaDeJogo passando SÓ o controlador
            telaDeJogo = new TelaDeJogo(voltarCallback, onVitoriaCallback, controlador);
            telaAtual = TelaAtiva.JOGO;
            Gdx.input.setInputProcessor(telaDeJogo.getMultiplexer());
        };



        // Callback para a TelaDeSelecao Voltar para a Inicial
        Runnable voltarParaInicialCallback = () -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
        };

        // 2. Instanciamos a telaDeSelecao (antes estava null)
        telaDeSelecao = new TelaDeSelecaoDeJogadores(voltarParaInicialCallback, iniciarJogoCallback);

        // Cria tela inicial com callback para iniciar o jogo
        telaInicial = new TelaInicial(
            () -> { // Callback "Iniciar Jogo"
                // Agora, o botão "Jogar" apenas navega para a TelaDeSelecao.
                telaDeSelecao.resetarEstado();
                telaAtual = TelaAtiva.SELECAO;
                Gdx.input.setInputProcessor(telaDeSelecao.stage);
            },
            () -> { // Callback "Regras"
                telaAtual = TelaAtiva.REGRAS;
                Gdx.input.setInputProcessor(telaDeRegras.stage);
            },
            () -> { // Callback "Config"
                telaAtual = TelaAtiva.CONFIG;
                Gdx.input.setInputProcessor(telaDeConfig.stage);
            }
        );

        telaDeRegras = new TelaDeRegras(() -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
        });

        telaDeConfig = new TelaDeConfig(() -> {
            telaAtual = TelaAtiva.INICIAL;
            Gdx.input.setInputProcessor(telaInicial.stage);
        });


        telaAtual = TelaAtiva.INICIAL;
        Gdx.input.setInputProcessor(telaInicial.stage);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        switch (telaAtual) {
            case INICIAL:
                telaInicial.render(delta);
                break;
            // --- MUDANÇA AQUI ---
            // Adicionado o 'case' para renderizar a nova tela de seleção
            case SELECAO:
                telaDeSelecao.render(delta);
                break;
            // --- FIM DA MUDANÇA ---
            case JOGO:
                // Adicionada checagem para evitar crash, pois telaDeJogo pode ser null
                if (telaDeJogo != null) {
                    telaDeJogo.render(delta);
                }
                break;
            case REGRAS:
                telaDeRegras.render(delta);
                break;
            case CONFIG:
                telaDeConfig.render(delta);
                break;
            case VITORIA:
                if (telaVitoria != null) telaVitoria.render(delta);
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        telaInicial.resize(width, height);
        // --- MUDANÇA AQUI ---
        // Adicionado o resize para a tela de seleção (com checagem de null)
        if (telaDeSelecao != null) telaDeSelecao.resize(width, height);
        // --- FIM DA MUDANÇA ---
        // Adicionada checagem para evitar crash
        if (telaDeJogo != null) telaDeJogo.resize(width, height);
        if (telaVitoria != null) telaVitoria.resize(width, height);
        telaDeRegras.resize(width, height);
        telaDeConfig.resize(width, height);
    }

    @Override
    public void dispose() {
        telaInicial.dispose();
        // --- MUDANÇA AQUI ---
        // Adicionado o dispose para a tela de seleção (com checagem de null)
        if (telaDeSelecao != null) telaDeSelecao.dispose();
        // --- FIM DA MUDANÇA ---
        // Adicionada checagem para evitar crash
        if (telaDeJogo != null) telaDeJogo.dispose();
        telaDeRegras.dispose();
        telaDeConfig.dispose();
        if (telaVitoria != null) telaVitoria.dispose();
    }
}
