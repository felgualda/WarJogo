package com.gruposete.war.ui; // Ajuste o pacote se necessário

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gruposete.war.core.Carta;
import com.gruposete.war.core.ControladorDePartida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uma classe segregada que é uma subjanela (Dialog) para
 * gerenciar a visualização e troca de cartas.
 */
public class DialogoCartas extends Dialog {

    private final ControladorDePartida controlador;
    private final List<Carta> maoDoJogador;

    // Estado interno do diálogo
    private final List<Carta> cartasSelecionadas = new ArrayList<>();
    private final Map<Carta, Actor> mapaAtores = new HashMap<>();
    private final List<Texture> texturasCarregadas = new ArrayList<>();

    private final Label errorLabel;

    public DialogoCartas(final ControladorDePartida controlador, Skin skin) {
        super("Suas Cartas", skin);

        this.controlador = controlador;
        this.maoDoJogador = controlador.getJogadorAtual().getCartas();

        // Configurações do Diálogo
        setModal(true);
        setMovable(true);

        // Label para erros
        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);

        // 1. Cria o container das cartas (Fileira única)
        Table containerCartas = new Table();
        if (maoDoJogador.isEmpty()) {
            containerCartas.add(new Label("Voce nao possui cartas.", skin));
        } else {
            // Loop para criar um Ator (Image) para cada Carta
            for (final Carta carta : maoDoJogador) {
                try {
                    // Carrega a textura da carta
                    Texture tex = new Texture(Gdx.files.internal(carta.getAssetPath()));
                    texturasCarregadas.add(tex); // Adiciona para futuro dispose

                    final Image atorCarta = new Image(tex);
                    atorCarta.setScale(1.0f); // Escala padrão
                    mapaAtores.put(carta, atorCarta); // Linka o dado ao ator

                    // --- Lógica de Seleção ---
                    atorCarta.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            errorLabel.setText(""); // Limpa o erro
                            if (cartasSelecionadas.contains(carta)) {
                                // --- DESELECIONAR ---
                                cartasSelecionadas.remove(carta);
                                atorCarta.setPosition((atorCarta.getX()+(atorCarta.getWidth()/22)), (atorCarta.getY()+(atorCarta.getHeight()/22)));
                                atorCarta.setScale(1.0f); // Feedback visual
                            } else {
                                // --- SELECIONAR ---
                                if (cartasSelecionadas.size() >= 3) {
                                    // Regra FIFO: Selecionar o 4º deseleciona o 1º
                                    Carta cartaRemovida = cartasSelecionadas.remove(0);
                                    Actor atorRemovido = mapaAtores.get(cartaRemovida);
                                    if (atorRemovido != null) {
                                        atorCarta.setPosition((atorCarta.getX()+(atorCarta.getWidth()/22)), (atorCarta.getY()+(atorCarta.getHeight()/22)));
                                        atorRemovido.setScale(1.0f);
                                    }
                                }
                                cartasSelecionadas.add(carta);
                                atorCarta.setScale(1.1f); // Feedback visual (10% maior)
                                atorCarta.setPosition((atorCarta.getX()-(atorCarta.getWidth()/22)), (atorCarta.getY()-(atorCarta.getHeight()/22)));
                            }
                        }
                    });

                    containerCartas.add(atorCarta).size(100, 150).pad(10); // Tamanho da carta (ajuste)

                } catch (Exception e) {
                    Gdx.app.error("DialogoCartas", "Falha ao carregar asset: " + carta.getAssetPath(), e);
                    containerCartas.add(new Label("Erro", skin)).pad(10);
                }
            }
        }

        // 2. Cria o ScrollPane
        ScrollPane scrollPane = new ScrollPane(containerCartas, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true); // Scroll horizontal

        // 3. Monta o Layout do Diálogo
        Table contentTable = getContentTable();
        contentTable.add(scrollPane).prefHeight(240).prefWidth(600); // Tamanho da área de scroll
        contentTable.row();
        contentTable.add(errorLabel).pad(10); // Área para mensagens de erro

        // 4. Botões "Voltar" e "Confirmar"
        TextButton btnVoltar = new TextButton("Voltar", skin);
        TextButton btnConfirmar = new TextButton("Confirmar", skin);

        button(btnVoltar, false); // 'false' é o objeto de resultado
        button(btnConfirmar, true); // 'true' é o objeto de resultado
    }

    /**
     * Chamado quando um dos botões (Voltar, Confirmar) é clicado.
     */
    @Override
    protected void result(Object object) {
        // Checa se o botão clicado foi "Confirmar" (resultado 'true')
        if (object.equals(true)) {
            if (cartasSelecionadas.size() != 3) {
                errorLabel.setText("Voce deve selecionar 3 cartas.");
                return; // Impede o diálogo de fechar
            }

            // --- Chama o Serviço (Controlador) ---
            boolean sucesso = controlador.tentarTrocaDeCartas(cartasSelecionadas);

            if (!sucesso) {
                errorLabel.setText("Combinacao Invalida.");
                return; // Impede o diálogo de fechar
            }
        }

        // Se foi "Voltar" (resultado 'false') ou se a troca foi bem-sucedida,
        // o diálogo continua e chama o hide().
    }

    /**
     * Sobrescrevemos o 'hide()' para limpar as texturas que carregamos
     * toda vez que o diálogo é fechado.
     */
    @Override
    public void hide() {
        super.hide(); // Chama o 'hide()' original

        // Limpeza de Memória (CRÍTICO)
        Gdx.app.log("DialogoCartas", "Fechando e limpando " + texturasCarregadas.size() + " texturas.");
        for (Texture t : texturasCarregadas) {
            t.dispose();
        }
    }
}
