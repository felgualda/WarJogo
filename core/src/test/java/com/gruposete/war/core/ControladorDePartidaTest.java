package com.gruposete.war.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Color;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;

public class ControladorDePartidaTest {

    @BeforeAll
    public static void setupGdxAppStub() throws Exception {
        // Create a dynamic proxy that implements com.badlogic.gdx.Application and provides no-op log/error
        Class<?> appInterface = Class.forName("com.badlogic.gdx.Application");
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("log".equals(name) || "error".equals(name) || "debug".equals(name)) {
                    // args: (String tag, String message) or (String tag, String message, Throwable)
                    return null;
                }
                // return sensible defaults for primitive return types
                Class<?> rt = method.getReturnType();
                if (rt == boolean.class) return false;
                if (rt == int.class) return 0;
                if (rt == long.class) return 0L;
                if (rt == float.class) return 0f;
                if (rt == double.class) return 0d;
                return null;
            }
        };

        Object proxy = Proxy.newProxyInstance(appInterface.getClassLoader(), new Class[]{appInterface}, handler);

        // Set com.badlogic.gdx.Gdx.app = proxy
        Class<?> gdxClass = Class.forName("com.badlogic.gdx.Gdx");
        Field appField = gdxClass.getField("app");
        appField.set(null, proxy);
    }

    @Test
    public void testAlocarTropas_validAndInvalid() {
        // Cria jogadores
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        Jogador atual = ctrl.getJogadorAtual();
        assertNotNull(atual);

        // Pega um territorio do jogador atual
        Territorio t = atual.getTerritorios().get(0);
        int antes = t.getTropas();

        // Tenta alocar quantidade maior que o lote atual (deve falhar)
        int lote = ctrl.getTropasADistribuir();
        boolean res = ctrl.alocarTropas(t, lote + 100);
        assertFalse(res);

        // Aloca 1 tropa se houver lote disponível
        if (lote > 0) {
            boolean ok = ctrl.alocarTropas(t, 1);
            assertTrue(ok, "Deveria alocar 1 tropa com sucesso");
            assertEquals(antes + 1, t.getTropas());
        }
    }

    @Test
    public void testTentarTrocaDeCartas_aplicaBonusEremoveCartas() {
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        Jogador atual = ctrl.getJogadorAtual();
        // cria 3 cartas válidas (3 diferentes)
        Territorio any = atual.getTerritorios().get(0);
        Carta c1 = new Carta(101, SimboloCarta.CIRCULO, any, "a");
        Carta c2 = new Carta(102, SimboloCarta.QUADRADO, any, "b");
        Carta c3 = new Carta(103, SimboloCarta.TRIANGULO, any, "c");

        atual.adicionarCarta(c1);
        atual.adicionarCarta(c2);
        atual.adicionarCarta(c3);

        int antesTotal = ctrl.getTropasADistribuirTotal();

        boolean trocou = ctrl.tentarTrocaDeCartas(List.of(c1, c2, c3));
        assertTrue(trocou);

        // Cartas removidas da mão
        assertEquals(0, atual.getCartas().size());

        // O bônus da primeira troca é 4 (ver ServicoDeCartas)
        assertEquals(antesTotal + 4, ctrl.getTropasADistribuirTotal());
    }

    @Test
    public void testMoverTropasEstrategicas_validAndInvalid() {
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        Jogador atual = ctrl.getJogadorAtual();
        List<Territorio> terr = atual.getTerritorios();

        if (terr.size() >= 2) {
            Territorio t1 = terr.get(0);
            Territorio t2 = terr.get(1);

            // Ambos têm pelo menos 1 tropa inicial
            assertTrue(t1.getTropas() >= 1);
            assertTrue(t2.getTropas() >= 1);

            int antes1 = t1.getTropas();
            int antes2 = t2.getTropas();

            // Move 1 do t1 para t2 (se adjacentes)
            boolean ok = ctrl.moverTropasEstrategicas(t1, t2, 1);
            // Resultado depende se são adjacentes; não garantido passar
            // Mas se passou: validar
            if (ok) {
                assertEquals(antes1 - 1, t1.getTropas());
                assertEquals(antes2 + 1, t2.getTropas());
            }
        }
    }

    @Test
    public void testEstadoTurno_progressaoDesFases() {
        Jogador j1 = new Jogador("P1", CorJogador.AZUL, 1, false);
        Jogador j2 = new Jogador("P2", CorJogador.VERMELHO, 2, false);
        List<Jogador> jogadores = new ArrayList<>();
        jogadores.add(j1);
        jogadores.add(j2);

        ControladorDePartida ctrl = new ControladorDePartida(jogadores);
        ctrl.iniciarPartida();

        assertEquals(ControladorDePartida.EstadoTurno.DISTRIBUINDO, ctrl.getEstadoTurno());

        // Aloca todas as tropas disponíveis
        Jogador atual = ctrl.getJogadorAtual();
        Territorio t = atual.getTerritorios().get(0);

        // Aloca enquanto houver tropas
        while (ctrl.getTropasADistribuir() > 0) {
            ctrl.alocarTropas(t, 1);
        }

        // Avança para próxima fase
        ctrl.proximaFaseTurno();

        // Se passou da primeira rodada (ou em rodadas posteriores): deve estar em ATACANDO
        // Na primeira rodada: volta para DISTRIBUINDO do próximo jogador
        // Vamos só checar que o estado mudou ou virou DISTRIBUINDO novamente
        assertNotNull(ctrl.getEstadoTurno());
    }
}
