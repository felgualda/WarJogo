package com.gruposete.war.ui;

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

public class DialogoCartas extends Dialog {

    // --- CONSTANTES ---
    private static final float CARD_WIDTH = 100f;
    private static final float CARD_HEIGHT = 150f;
    private static final float CARD_PAD = 10f;
    private static final float SCROLL_W = 600f;
    private static final float SCROLL_H = 240f;
    private static final float FONT_SCALE_BTN = 1.2f;

    // Escala e Ajuste Manual (O "Magic Number" 22)
    private static final float SCALE_NORMAL = 1.0f;
    private static final float SCALE_SELECTED = 1.1f;
    private static final float OFFSET_DIVISOR = 22f;
    private static final float BTN_PAD = 20f;

    // --- ATRIBUTOS ---
    private final ControladorDePartida controlador;
    private final List<Carta> maoDoJogador;
    private final Label errorLabel;

    // --- ESTADO INTERNO ---
    private final List<Carta> cartasSelecionadas = new ArrayList<>();
    // Alterado para Map<Carta, Image> para facilitar acesso aos métodos de ator
    private final Map<Carta, Image> mapaAtores = new HashMap<>();
    private final List<Texture> texturasCarregadas = new ArrayList<>();

    public DialogoCartas(final ControladorDePartida controlador, Skin skin) {
        super("Suas Cartas", skin);
        this.controlador = controlador;
        this.maoDoJogador = controlador.getJogadorAtual().getCartas();

        setModal(true);
        setMovable(true);

        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);

        construirInterface(skin);
    }

    private void construirInterface(Skin skin) {
        // 1. Container de Cartas
        Table containerCartas = new Table();

        if (maoDoJogador.isEmpty()) {
            containerCartas.add(new Label("Voce nao possui cartas.", skin));
        } else {
            for (final Carta carta : maoDoJogador) {
                adicionarCartaAoContainer(containerCartas, carta, skin);
            }
        }

        // 2. ScrollPane
        ScrollPane scrollPane = new ScrollPane(containerCartas, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true);

        // 3. Layout Principal
        Table contentTable = getContentTable();
        contentTable.add(scrollPane).prefWidth(SCROLL_W).prefHeight(SCROLL_H);
        contentTable.row();
        contentTable.add(errorLabel).pad(CARD_PAD);

        // 4. Botões
        TextButton btnVoltar = new TextButton("Voltar", skin);
        TextButton btnConfirmar = new TextButton("Confirmar", skin);
        btnVoltar.getLabel().setFontScale(FONT_SCALE_BTN);
        btnConfirmar.getLabel().setFontScale(FONT_SCALE_BTN);
        getButtonTable().defaults().pad(BTN_PAD);
        button(btnVoltar, false);
        button(btnConfirmar, true);
    }

    private void adicionarCartaAoContainer(Table container, final Carta carta, Skin skin) {
        try {
            Texture tex = new Texture(Gdx.files.internal(carta.getAssetPath()));
            texturasCarregadas.add(tex);

            final Image atorCarta = new Image(tex);
            atorCarta.setScale(SCALE_NORMAL);
            // NOTA: Não usamos setOrigin aqui, pois usaremos o cálculo manual

            mapaAtores.put(carta, atorCarta);

            atorCarta.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tratarCliqueCarta(carta, atorCarta);
                }
            });

            container.add(atorCarta).size(CARD_WIDTH, CARD_HEIGHT).pad(CARD_PAD);

        } catch (Exception e) {
            Gdx.app.error("DialogoCartas", "Falha ao carregar asset: " + carta.getAssetPath(), e);
            container.add(new Label("Erro", skin)).pad(CARD_PAD);
        }
    }

    /**
     * Aplica a lógica de seleção com o cálculo manual de posição.
     */
    private void tratarCliqueCarta(Carta carta, Image ator) {
        errorLabel.setText("");

        // Calcula o deslocamento necessário (W/22, H/22)
        float moveX = ator.getWidth() / OFFSET_DIVISOR;
        float moveY = ator.getHeight() / OFFSET_DIVISOR;

        if (cartasSelecionadas.contains(carta)) {
            // --- DESELECIONAR ---
            cartasSelecionadas.remove(carta);

            // Volta ao normal: Move para a direita/cima (+) e reduz escala
            ator.setPosition(ator.getX() + moveX, ator.getY() + moveY);
            ator.setScale(SCALE_NORMAL);

        } else {
            // --- SELECIONAR ---
            if (cartasSelecionadas.size() >= 3) {
                // Regra FIFO: Remove a primeira da lista
                Carta cartaRemovida = cartasSelecionadas.remove(0);
                Image atorRemovido = mapaAtores.get(cartaRemovida);

                if (atorRemovido != null) {
                    // Reseta visualmente a carta que foi removida (FIFO)
                    float oldMoveX = atorRemovido.getWidth() / OFFSET_DIVISOR;
                    float oldMoveY = atorRemovido.getHeight() / OFFSET_DIVISOR;

                    atorRemovido.setPosition(atorRemovido.getX() + oldMoveX, atorRemovido.getY() + oldMoveY);
                    atorRemovido.setScale(SCALE_NORMAL);
                }
            }

            cartasSelecionadas.add(carta);

            // Aplica destaque: Move para esquerda/baixo (-) e aumenta escala
            ator.setPosition(ator.getX() - moveX, ator.getY() - moveY);
            ator.setScale(SCALE_SELECTED);
        }
    }

    @Override
    protected void result(Object object) {
        if (object.equals(true)) {
            // Botão Confirmar
            if (cartasSelecionadas.size() != 3) {
                errorLabel.setText("Voce deve selecionar 3 cartas.");
                cancel();
                return;
            }

            boolean sucesso = controlador.tentarTrocaDeCartas(cartasSelecionadas);

            if (!sucesso) {
                errorLabel.setText("Combinacao Invalida.");
                cancel();
                return;
            }
        }
        // Se false (Voltar) ou sucesso, fecha.
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.app.log("DialogoCartas", "Limpando " + texturasCarregadas.size() + " texturas.");
        for (Texture t : texturasCarregadas) {
            t.dispose();
        }
    }
}
