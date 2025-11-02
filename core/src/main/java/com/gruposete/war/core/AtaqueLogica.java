package com.gruposete.war;

import java.util.Random;
import java.util.Arrays;
import java.util.Collections;



public class Ataque {
    private Territorio territorioAtacante;
    private Territorio territorioDefensor;
    private Jogador jogadorAtacante;
    private Jogador jogadorDefensor;
    private Random random = new Random();

    public Ataque(Territorio territorioA, Territorio territorioD){
        this.territorioAtacante = territorioA;
        this.territorioDefensor = territorioD;
    }

    public AtaqueEstado executarUmaRodada(){
        if (territorioAtacante.getTropas() <= 1) { return AtaqueEstado.TROPAS_INSUFICIENTES; }
        // if (territorioAtacante.isAdjacente(territorioDefensor) == false) { return }

        int qntdDadosAtacante = territorioAtacante.getTropas() - 1;
        int qntdDadosDefensor = territorioDefensor.getTropas();
        if (qntdDadosAtacante > 3) { qntdDadosAtacante = 3; }
        if (qntdDadosDefensor > 3) { qntdDadosDefensor = 3; }

        rolarDados(qntdDadosAtacante, qntdDadosDefensor);
            
        if (territorioAtacante.getTropas() == 1) { 
            return AtaqueEstado.TROPAS_INSUFICIENTES;
        }
        if (territorioDefensor.getTropas() == 0) {
            territorioDefensor.setPlayerId(territorioAtacante.getPlayerId());        // O controlador da partida deve atualizar as listas de territórios dos jogadores.
            return AtaqueEstado.TERRITORIO_CONQUISTADO;                              // O controlador da partida deve pedir ao usuário quantas tropas mover e, em seguida, chamar um método para decrementar o atacante e incrementar o defensor.
        }

        return AtaqueEstado.CONTINUAR_POSSIVEL;
    }

    private void rolarDados(int qntdDadosAtacante, int qntdDadosDefensor){

        Integer[] dadosAtacante = new Integer[3];
        Integer[] dadosDefensor = new Integer[3];
        
        // Rolagem dos dados

        for (int i = 0; i < qntdDadosAtacante; i++){
            dadosAtacante[i] = random.nextInt(6) + 1;
            Arrays.sort(dadosAtacante, Collections.reverseOrder());
        }

        for (int i = 0; i < qntdDadosDefensor; i++){
            dadosDefensor[i] = random.nextInt(6) + 1;
            Arrays.sort(dadosDefensor, Collections.reverseOrder());
        }
        
        // Verificar, para cada dado, quem ganhou

        for (int i = 0; i < qntdDadosAtacante; i++){
            if (dadosDefensor[i] != null){
                if (dadosDefensor[i] < dadosAtacante[i]) { 
                    territorioDefensor.decrementarTropas(); 
                } 
                else { 
                    territorioAtacante.decrementarTropas(); 
                }
            }

            else {
                territorioDefensor.decrementarTropas();
            }
        }
    }
}
