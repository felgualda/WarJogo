package com.gruposete.war;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.gruposete.war.ui.TelaDeConfig;
import com.gruposete.war.ui.TelaDeJogo;
import com.gruposete.war.ui.TelaDeRegras;
import com.gruposete.war.ui.TelaInicial;
import com.gruposete.war.ui.TelaDeSelecaoDeJogadores;

// Imports adicionados para a nova lógica de setup
import com.badlogic.gdx.utils.Array;
import com.gruposete.war.core.CorJogador;
import com.gruposete.war.core.Jogador;
import com.gruposete.war.core.SetupPartida;
import com.gruposete.war.core.Territorio;
import com.gruposete.war.core.Mapa;
import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private TelaInicial telaInicial;
    private TelaDeSelecaoDeJogadores telaDeSelecao;
    private TelaDeJogo telaDeJogo; // A telaDeJogo agora começa null
    private TelaDeRegras telaDeRegras;
    private TelaDeConfig telaDeConfig;


    private enum TelaAtiva { INICIAL, SELECAO, JOGO, REGRAS, CONFIG }
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
            // Pega a lista de jogadores da tela de seleção (não é mais uma simulação)
            List<Jogador> jogadores = telaDeSelecao.getJogadoresSelecionados();

            // Roda a lógica de Setup para preparar a partida
            SetupPartida setup = new SetupPartida(jogadores);
            List<Jogador> jogadoresProntos = setup.getJogadoresPreparados();
            Array<Territorio> territoriosProntos = setup.getTodosOsTerritorios();
            Mapa mapaAdjacenciaPronto = setup.getMapaAdjacencias(); // (Esta linha já estava no seu commit)

            // Cria tela de jogo com callback para voltar ao menu
            Runnable voltarCallback = () -> {
                telaAtual = TelaAtiva.INICIAL;
                Gdx.input.setInputProcessor(telaInicial.stage);
                // Descarta a tela de jogo antiga para liberar memória
                if (telaDeJogo != null) {
                    telaDeJogo.dispose();
                    telaDeJogo = null;
                }
            };

            // Cria a nova TelaDeJogo com os dados prontos (incluindo o mapa do seu commit)
            telaDeJogo = new TelaDeJogo(voltarCallback, jogadoresProntos, territoriosProntos, mapaAdjacenciaPronto);

            // Muda o estado do jogo
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
    }
}
