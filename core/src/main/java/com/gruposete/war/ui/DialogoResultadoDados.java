package com.gruposete.war.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.gruposete.war.core.ResultadoCombate;

public class DialogoResultadoDados extends Dialog {

    public DialogoResultadoDados(ResultadoCombate resultado, Skin skin) {
        super("Resultado do Combate", skin);
        
        // Configurações
        setModal(true);
        setMovable(false);
        
        Table content = getContentTable();
        content.pad(20);

        // --- COLUNA ATACANTE ---
        Table tableAtk = new Table();
        Label lblAtk = new Label("Ataque", skin);
        lblAtk.setColor(Color.RED);
        tableAtk.add(lblAtk).padBottom(10).row();
        
        for (Integer valor : resultado.dadosAtacante) {
            Label dado = new Label("[" + valor + "]", skin);
            dado.setFontScale(1.5f);
            tableAtk.add(dado).pad(5).row();
        }

        // --- CORREÇÃO DO ERRO DO PRINT ---
        // Errado: tableAtk.add(...).color(...) -> Célula não tem cor!
        // Certo: Cria o Label -> Pinta o Label -> Adiciona
        Label lblPerdasA = new Label("-" + resultado.perdasAtacante + " tropas", skin);
        lblPerdasA.setColor(Color.ORANGE); 
        tableAtk.add(lblPerdasA).padTop(10); 

        // --- COLUNA DEFENSOR ---
        Table tableDef = new Table();
        Label lblDef = new Label("Defesa", skin);
        lblDef.setColor(Color.YELLOW);
        tableDef.add(lblDef).padBottom(10).row();

        for (Integer valor : resultado.dadosDefensor) {
            Label dado = new Label("[" + valor + "]", skin);
            dado.setFontScale(1.5f);
            tableDef.add(dado).pad(5).row();
        }

        // --- CORREÇÃO DA SEGUNDA COLUNA ---
        Label lblPerdasD = new Label("-" + resultado.perdasDefensor + " tropas", skin);
        lblPerdasD.setColor(Color.ORANGE);
        tableDef.add(lblPerdasD).padTop(10);

        // --- VS ---
        Label vsLabel = new Label("VS", skin);
        vsLabel.setAlignment(Align.center);

        // Adiciona ao layout principal
        content.add(tableAtk).padRight(30);
        content.add(vsLabel).padRight(30);
        content.add(tableDef);

        // Botão OK
        button("OK");
    }
}