package com.gruposete.war.ui; // Ajuste o pacote se necessário

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Um widget SpinBox personalizado para scene2d.ui.
 * Combina um TextField com botões de incremento (+) e decremento (-).
 * Gerencia os limites (min/max) e a validação da entrada.
 */
public class SpinBox extends Table {

    private int value;
    private final int min;
    private final int max;
    private final int step = 1; // (Podemos tornar isso configurável mais tarde)

    private final TextField valueText;
    private final TextButton upButton;
    private final TextButton downButton;

    public SpinBox(int min, int max, Skin skin) {
        super(skin);
        this.min = min;
        this.max = max;
        this.value = min; // Começa no valor mínimo

        // 1. Criar os componentes
        valueText = new TextField(String.valueOf(value), skin);
        upButton = new TextButton("+", skin);
        downButton = new TextButton("-", skin);

        // 2. Adicionar filtros ao TextField
        // Garante que o usuário só pode digitar números
        valueText.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        // 3. Adicionar Listeners (A Lógica)

        // Listener do botão de diminuir
        downButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValueFromText(); // Pega o valor atual do texto
                setValue(value - step); // Diminui e re-valida
            }
        });

        // Listener do botão de aumentar
        upButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValueFromText(); // Pega o valor atual do texto
                setValue(value + step); // Aumenta e re-valida
            }
        });

        // Listener para quando o usuário digita e sai do campo
        valueText.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Isso é chamado quando o usuário aperta Enter ou perde o foco
                updateValueFromText();
            }
        });

        // 4. Adicionar componentes à Tabela (Layout)
        // [ - ] [ 123 ] [ + ]
        add(downButton).width(40).height(40);
        add(valueText).width(60).center();
        add(upButton).width(40).height(40);
    }

    /**
     * Define o valor, garantindo que ele esteja dentro dos limites min/max.
     */
    public void setValue(int newValue) {
        // Validação (Clamp)
        if (newValue > max) {
            newValue = max;
        }
        if (newValue < min) {
            newValue = min;
        }
        this.value = newValue;

        // Atualiza o texto para refletir o valor validado
        updateText();
    }

    /**
     * Lê o valor do TextField e o valida.
     * Chamado antes de qualquer operação de incremento/decremento ou de get.
     */
    private void updateValueFromText() {
        try {
            int textValue = Integer.parseInt(valueText.getText());
            setValue(textValue);
        } catch (NumberFormatException e) {
            // Se o usuário digitou algo inválido (ex: "abc" ou ""),
            // reseta para o último valor válido.
            setValue(this.value);
        }
    }

    /**
     * Atualiza o TextField para mostrar o valor atual.
     */
    private void updateText() {
        valueText.setText(String.valueOf(this.value));
    }

    /**
     * Pega o valor numérico atual da SpinBox.
     * @return O valor validado.
     */
    public int getValue() {
        // Garante que o valor do texto seja lido antes de retornar
        updateValueFromText();
        return this.value;
    }
}
