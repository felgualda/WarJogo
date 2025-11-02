package com.gruposete.war.core; // Ajuste o pacote se necessário

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import java.util.List;
// import java.util.Random; // Não é mais necessário, AtaqueLogica tem o seu

/**
 * Gerencia o estado da partida, fluxo de turnos e regras do jogo.
 * ATUALIZADO: Usa a classe AtaqueLogica oficial e o termo 'Tropas'.
 */
public class ControladorDePartida {

    // --- Estado do Jogo ---
    private List<Jogador> jogadores;
    private Array<Territorio> territorios;
    private Mapa mapa;
    private SetupPartida setupLogic;

    private int indiceJogadorAtual;
    private Jogador jogadorAtual;
    private int tropasADistribuir;

    // --- Estado do Turno ---
    public enum EstadoTurno {
        DISTRIBUINDO,
        ATACANDO,
        MOVIMENTANDO
    }

    private EstadoTurno estadoTurno;
    private boolean conquistouTerritorioNesteTurno;


    /**
     * Construtor. Recebe jogadores da TelaDeSelecao.
     */
    public ControladorDePartida(List<Jogador> jogadoresSelecionados) {
        this.jogadores = jogadoresSelecionados;
        // O 'Random' não é mais necessário aqui; AtaqueLogica gerencia o seu.
    }

    /**
     * Executa o setup inicial do jogo.
     * Chamado pela Main ao iniciar o jogo.
     */
    public void iniciarPartida() {
        this.setupLogic = new SetupPartida(this.jogadores);

        this.jogadores = setupLogic.getJogadoresPreparados();
        this.territorios = setupLogic.getTodosOsTerritorios();
        this.mapa = setupLogic.getMapaAdjacencias();

        this.indiceJogadorAtual = 0;
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);

        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;

        calcularTropasDoTurno();
    }

    // --- LÓGICA DE FLUXO DE JOGO ---

    /**
     * Avança para o próximo jogador e aplica regras de fim de turno.
     */
    public void passarAVez() {
        if (this.conquistouTerritorioNesteTurno) {
            darCartaAoJogadorAtual();
        }

        this.indiceJogadorAtual = (this.indiceJogadorAtual + 1) % this.jogadores.size();
        this.jogadorAtual = this.jogadores.get(this.indiceJogadorAtual);

        this.estadoTurno = EstadoTurno.DISTRIBUINDO;
        this.conquistouTerritorioNesteTurno = false;

        calcularTropasDoTurno();
    }

    /**
     * Avança para a próxima fase do turno.
     */
    public void proximaFaseTurno() {
        switch (this.estadoTurno) {
            case DISTRIBUINDO:
                this.estadoTurno = EstadoTurno.ATACANDO;
                break;
            case ATACANDO:
                this.estadoTurno = EstadoTurno.MOVIMENTANDO;
                break;
            case MOVIMENTANDO:
                passarAVez();
                break;
        }
    }
    private void calcularTropasDoTurno() {
        // TODO: Implementar a lógica real min(Territorios / 2 + Bônus de Continente, 3)
        // Por enquanto, damos 5 tropas de bônus para teste.
        this.tropasADistribuir = 5;
    }
    public boolean alocarTropa(Territorio territorio) {
        if (this.estadoTurno != EstadoTurno.DISTRIBUINDO ||
            this.tropasADistribuir <= 0 ||
            territorio.getPlayerId()-1 != jogadores.indexOf(this.jogadorAtual))
        {
            return false; // Não pode alocar
        }

        territorio.setTropas(territorio.getTropas() + 1);
        this.tropasADistribuir--;
        return true;
    }
    /**
     * Executa UMA rodada de combate usando AtaqueLogica.
     * @param atacante Território de origem do ataque.
     * @param defensor Território alvo.
     * @return O AtaqueEstado (ex: TERRITORIO_CONQUISTADO, TROPAS_INSUFICIENTES)
     */
    public AtaqueEstado realizarAtaque(Territorio atacante, Territorio defensor) {

        // 1. Obter o jogador defensor
        int jogadorDefensorID = defensor.getPlayerId();

        // 2. Criar a lógica de ataque para esta rodada
        AtaqueLogica logica = new AtaqueLogica(atacante, defensor, this.jogadorAtual, jogadores.get(jogadorDefensorID-1), this.mapa);
        // 3. Executar a rodada (AtaqueLogica atualiza as tropas nos objetos)
        AtaqueEstado resultado = logica.executarUmaRodada();

        // 4. Atualizar o estado do turno se houve conquista
        if (resultado == AtaqueEstado.TERRITORIO_CONQUISTADO) {
            this.conquistouTerritorioNesteTurno = true;

            // NOTA: O AtaqueLogica já define o 'playerId' no território.
            // A TelaDeJogo agora deve perguntar "Quantas tropas mover?"
            // e então chamar moverTropasAposConquista().
        }

        return resultado; // Retorna o enum
    }

    /**
     * Move as tropas após uma conquista.
     * A TelaDeJogo deve chamar este método após perguntar ao usuário a quantidade.
     * @param origem O território que atacou.
     * @param destino O território conquistado (que agora tem 0 tropas).
     * @param tropasParaMover A quantidade que o usuário escolheu.
     */
    public void moverTropasAposConquista(Territorio origem, Territorio destino, int tropasParaMover) {
        if (tropasParaMover < 1) return;
        if (tropasParaMover > 3) return;

        destino.setTropas(destino.getTropas() + tropasParaMover); // (Será 0 + tropas)
        origem.setTropas(origem.getTropas() - tropasParaMover);
    }

    /**
     * Adiciona uma carta (mock) ao jogador atual.
     */
    private void darCartaAoJogadorAtual() {
        Carta novaCarta = new Carta(1, "Cartas/1.png"); // Mock
        this.jogadorAtual.getCartas().add(novaCarta);
    }


    // --- GETTERS (Para a TelaDeJogo ler o estado) ---

    public List<Jogador> getJogadores() { return jogadores; }
    public Array<Territorio> getTerritorios() { return territorios; }
    public Mapa getMapa() { return mapa; }
    public Jogador getJogadorAtual() { return jogadorAtual; }
    public EstadoTurno getEstadoTurno() { return estadoTurno; }
    public int getTropasADistribuir() { return tropasADistribuir; }
    // A classe interna 'ResultadoCombate' foi removida pois é desnecessária.
    // A TelaDeJogo deve reagir ao 'AtaqueEstado' retornado por 'realizarAtaque'.
}
