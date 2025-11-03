package com.gruposete.war.core;

import java.util.Map;
import java.util.HashMap;
import com.badlogic.gdx.utils.Array;
import java.util.List; 
import java.util.ArrayList;

public class Mapa {
    private Map<Territorio, Array<Territorio>> adjacencias;     // Mapa que armazena as adjacências entre os territórios
    private Map<String, Territorio> tNomes;                     // Mapa para buscar territórios pelo nome

    private Map<Continente, List<Territorio>> territoriosPorContinente;

    // Construtor: inicializa o mapa com a lista de territórios
    public Mapa(Array<Territorio> territorios){
        this.tNomes = new HashMap<>();

        // Preenche o mapa de nomes para facilitar a busca
        for (Territorio t : territorios) {
            this.tNomes.put(t.getNome(), t);
        }

        this.territoriosPorContinente = new HashMap<>();
        setupContinentes();

        // Inicializa a estrutura de adjacências
        this.adjacencias = inicializarMapa(territorios);
    }

    private void setupContinentes() {
        // América do Norte (9 Territórios)
        adicionarAoContinente("Alasca", Continente.AMERICA_NORTE);
        adicionarAoContinente("Mackenzie", Continente.AMERICA_NORTE);
        adicionarAoContinente("Vancouver", Continente.AMERICA_NORTE);
        adicionarAoContinente("Ottawa", Continente.AMERICA_NORTE);
        adicionarAoContinente("Labrador", Continente.AMERICA_NORTE);
        adicionarAoContinente("Groenlândia", Continente.AMERICA_NORTE);
        adicionarAoContinente("Nova Iorque", Continente.AMERICA_NORTE);
        adicionarAoContinente("California", Continente.AMERICA_NORTE);
        adicionarAoContinente("Mexico", Continente.AMERICA_NORTE);

        // América do Sul (4 Territórios)
        adicionarAoContinente("Venezuela", Continente.AMERICA_SUL);
        adicionarAoContinente("Peru", Continente.AMERICA_SUL);
        adicionarAoContinente("Brasil", Continente.AMERICA_SUL);
        adicionarAoContinente("Argentina", Continente.AMERICA_SUL);

        // África (6 Territórios)
        adicionarAoContinente("Argelia", Continente.AFRICA);
        adicionarAoContinente("Congo", Continente.AFRICA);
        adicionarAoContinente("Africa do Sul", Continente.AFRICA);
        adicionarAoContinente("Sudão", Continente.AFRICA);
        adicionarAoContinente("Egito", Continente.AFRICA);
        adicionarAoContinente("Madagascar", Continente.AFRICA);

        // Europa (7 Territórios)
        adicionarAoContinente("Islândia", Continente.EUROPA);
        adicionarAoContinente("Inglaterra", Continente.EUROPA);
        adicionarAoContinente("França", Continente.EUROPA);
        adicionarAoContinente("Alemanha", Continente.EUROPA);
        adicionarAoContinente("Polônia", Continente.EUROPA);
        adicionarAoContinente("Moscou", Continente.EUROPA);
        adicionarAoContinente("Suécia", Continente.EUROPA);

        // Ásia (12 Territórios)
        adicionarAoContinente("Oriente Médio", Continente.ASIA);
        adicionarAoContinente("India", Continente.ASIA);
        adicionarAoContinente("Aral", Continente.ASIA);
        adicionarAoContinente("Omsk", Continente.ASIA);
        adicionarAoContinente("Dudinka", Continente.ASIA);
        adicionarAoContinente("Mongólia", Continente.ASIA);
        adicionarAoContinente("Tchita", Continente.ASIA);
        adicionarAoContinente("China", Continente.ASIA);
        adicionarAoContinente("Vietnã", Continente.ASIA);
        adicionarAoContinente("Japão", Continente.ASIA);
        adicionarAoContinente("Vladvostok", Continente.ASIA);
        adicionarAoContinente("Sibéria", Continente.ASIA);

        // Oceania (4 Territórios)
        adicionarAoContinente("Australia", Continente.OCEANIA);
        adicionarAoContinente("Nova Guiné", Continente.OCEANIA);
        adicionarAoContinente("Sumatra", Continente.OCEANIA);
        adicionarAoContinente("Borneo", Continente.OCEANIA);
    }

    // Método auxiliar (Privado)
    private void adicionarAoContinente(String nomeTerritorio, Continente continente) {
        Territorio t = tNomes.get(nomeTerritorio);
        if (t != null) {
            // Inicializa a lista para o Continente se ainda não existir
            territoriosPorContinente.computeIfAbsent(continente, k -> new ArrayList<>()).add(t);
        }
    }

    // Método que cria e configura todas as adjacências entre territórios
    private Map<Territorio, Array<Territorio>> inicializarMapa(Array<Territorio> territorios){
        // Criando o mapa de adjacências
        Map<Territorio, Array<Territorio>> mapa = new HashMap<>();

        // América do Norte
        Array<Territorio> alaskaAdj = new Array<>();
        alaskaAdj.add(tNomes.get("Mackenzie"));
        alaskaAdj.add(tNomes.get("Vancouver"));
        mapa.put(tNomes.get("Alasca"), alaskaAdj);

        Array<Territorio> mackenzieAdj = new Array<>();
        mackenzieAdj.add(tNomes.get("Alasca"));
        mackenzieAdj.add(tNomes.get("Vancouver"));
        mackenzieAdj.add(tNomes.get("Ottawa"));
        mackenzieAdj.add(tNomes.get("Groenlândia"));
        mapa.put(tNomes.get("Mackenzie"), mackenzieAdj);

        Array<Territorio> vancouverAdj = new Array<>();
        vancouverAdj.add(tNomes.get("Alasca"));
        vancouverAdj.add(tNomes.get("Mackenzie"));
        vancouverAdj.add(tNomes.get("Ottawa"));
        vancouverAdj.add(tNomes.get("California"));
        mapa.put(tNomes.get("Vancouver"), vancouverAdj);

        Array<Territorio> ottawaAdj = new Array<>();
        ottawaAdj.add(tNomes.get("Vancouver"));
        ottawaAdj.add(tNomes.get("Mackenzie"));
        ottawaAdj.add(tNomes.get("Labrador"));
        ottawaAdj.add(tNomes.get("Nova Iorque"));
        ottawaAdj.add(tNomes.get("California"));
        mapa.put(tNomes.get("Ottawa"), ottawaAdj);

        Array<Territorio> labradorAdj = new Array<>();
        labradorAdj.add(tNomes.get("Ottawa"));
        labradorAdj.add(tNomes.get("Groenlândia"));
        labradorAdj.add(tNomes.get("Nova Iorque"));
        mapa.put(tNomes.get("Labrador"), labradorAdj);

        Array<Territorio> groenlandiaAdj = new Array<>();
        groenlandiaAdj.add(tNomes.get("Labrador"));
        groenlandiaAdj.add(tNomes.get("Mackenzie"));
        groenlandiaAdj.add(tNomes.get("Islândia"));
        mapa.put(tNomes.get("Groenlândia"), groenlandiaAdj);

        Array<Territorio> novaiorqueAdj = new Array<>();
        novaiorqueAdj.add(tNomes.get("Ottawa"));
        novaiorqueAdj.add(tNomes.get("California"));
        novaiorqueAdj.add(tNomes.get("Mexico"));
        novaiorqueAdj.add(tNomes.get("Labrador"));
        mapa.put(tNomes.get("Nova Iorque"), novaiorqueAdj);

        Array<Territorio> californiaAdj = new Array<>();
        californiaAdj.add(tNomes.get("Vancouver"));
        californiaAdj.add(tNomes.get("Ottawa"));
        californiaAdj.add(tNomes.get("Nova Iorque"));
        californiaAdj.add(tNomes.get("Mexico"));
        mapa.put(tNomes.get("California"), californiaAdj);

        Array<Territorio> mexicoAdj = new Array<>();
        mexicoAdj.add(tNomes.get("California"));
        mexicoAdj.add(tNomes.get("Nova Iorque"));
        mexicoAdj.add(tNomes.get("Venezuela"));
        mapa.put(tNomes.get("Mexico"), mexicoAdj);

        // América do Sul
        Array<Territorio> venezuelaAdj = new Array<>();
        venezuelaAdj.add(tNomes.get("Mexico"));
        venezuelaAdj.add(tNomes.get("Peru"));
        venezuelaAdj.add(tNomes.get("Brasil"));
        mapa.put(tNomes.get("Venezuela"), venezuelaAdj);

        Array<Territorio> peruAdj = new Array<>();
        peruAdj.add(tNomes.get("Venezuela"));
        peruAdj.add(tNomes.get("Brasil"));
        peruAdj.add(tNomes.get("Argentina"));
        mapa.put(tNomes.get("Peru"), peruAdj);

        Array<Territorio> brasilAdj = new Array<>();
        brasilAdj.add(tNomes.get("Venezuela"));
        brasilAdj.add(tNomes.get("Peru"));
        brasilAdj.add(tNomes.get("Argentina"));
        brasilAdj.add(tNomes.get("Argelia"));
        mapa.put(tNomes.get("Brasil"), brasilAdj);

        Array<Territorio> argentinaAdj = new Array<>();
        argentinaAdj.add(tNomes.get("Peru"));
        argentinaAdj.add(tNomes.get("Brasil"));
        mapa.put(tNomes.get("Argentina"), argentinaAdj);

        // África
        Array<Territorio> argeliaAdj = new Array<>();
        argeliaAdj.add(tNomes.get("Brasil"));
        argeliaAdj.add(tNomes.get("Congo"));
        argeliaAdj.add(tNomes.get("Sudão"));
        argeliaAdj.add(tNomes.get("Egito"));
        argeliaAdj.add(tNomes.get("França"));
        mapa.put(tNomes.get("Argelia"), argeliaAdj);

        Array<Territorio> congoAdj = new Array<>();
        congoAdj.add(tNomes.get("Argelia"));
        congoAdj.add(tNomes.get("Sudão"));
        congoAdj.add(tNomes.get("Africa do Sul"));
        mapa.put(tNomes.get("Congo"), congoAdj);

        Array<Territorio> africadosulAdj = new Array<>();
        africadosulAdj.add(tNomes.get("Congo"));
        africadosulAdj.add(tNomes.get("Sudão"));
        africadosulAdj.add(tNomes.get("Madagascar"));
        mapa.put(tNomes.get("Africa do Sul"), africadosulAdj);

        Array<Territorio> sudaoAdj = new Array<>();
        sudaoAdj.add(tNomes.get("Argelia"));
        sudaoAdj.add(tNomes.get("Congo"));
        sudaoAdj.add(tNomes.get("Africa do Sul"));
        sudaoAdj.add(tNomes.get("Egito"));
        sudaoAdj.add(tNomes.get("Oriente Médio"));
        sudaoAdj.add(tNomes.get("Madagascar"));
        mapa.put(tNomes.get("Sudão"), sudaoAdj);

        Array<Territorio> egitoAdj = new Array<>();
        egitoAdj.add(tNomes.get("Argelia"));
        egitoAdj.add(tNomes.get("Sudão"));
        egitoAdj.add(tNomes.get("Oriente Médio"));
        mapa.put(tNomes.get("Egito"), egitoAdj);

        Array<Territorio> madagascarAdj = new Array<>();
        madagascarAdj.add(tNomes.get("Africa do Sul"));
        madagascarAdj.add(tNomes.get("Sudão"));
        mapa.put(tNomes.get("Madagascar"), madagascarAdj);

        // Europa
        Array<Territorio> islandiaAdj = new Array<>();
        islandiaAdj.add(tNomes.get("Groenlândia"));
        islandiaAdj.add(tNomes.get("Inglaterra"));
        mapa.put(tNomes.get("Islândia"), islandiaAdj);

        Array<Territorio> inglaterraAdj = new Array<>();
        inglaterraAdj.add(tNomes.get("França"));
        inglaterraAdj.add(tNomes.get("Alemanha"));
        inglaterraAdj.add(tNomes.get("Suécia"));
        inglaterraAdj.add(tNomes.get("Islândia"));
        mapa.put(tNomes.get("Inglaterra"), inglaterraAdj);

        Array<Territorio> francaAdj = new Array<>();
        francaAdj.add(tNomes.get("Inglaterra"));
        francaAdj.add(tNomes.get("Alemanha"));
        francaAdj.add(tNomes.get("Polônia"));
        francaAdj.add(tNomes.get("Argelia"));
        mapa.put(tNomes.get("França"), francaAdj);

        Array<Territorio> alemanhaAdj = new Array<>();
        alemanhaAdj.add(tNomes.get("Inglaterra"));
        alemanhaAdj.add(tNomes.get("França"));
        alemanhaAdj.add(tNomes.get("Polônia"));
        alemanhaAdj.add(tNomes.get("Suécia"));
        mapa.put(tNomes.get("Alemanha"), alemanhaAdj);

        Array<Territorio> poloniaAdj = new Array<>();
        poloniaAdj.add(tNomes.get("França"));
        poloniaAdj.add(tNomes.get("Alemanha"));
        poloniaAdj.add(tNomes.get("Moscou"));
        mapa.put(tNomes.get("Polônia"), poloniaAdj);

        Array<Territorio> moscouAdj = new Array<>();
        moscouAdj.add(tNomes.get("Suécia"));
        moscouAdj.add(tNomes.get("Aral"));
        moscouAdj.add(tNomes.get("Omsk"));
        moscouAdj.add(tNomes.get("Oriente Médio"));
        mapa.put(tNomes.get("Moscou"), moscouAdj);

        Array<Territorio> sueciaAdj = new Array<>();
        sueciaAdj.add(tNomes.get("Inglaterra"));
        sueciaAdj.add(tNomes.get("Moscou"));
        sueciaAdj.add(tNomes.get("Alemanha"));
        mapa.put(tNomes.get("Suécia"), sueciaAdj);

        // Ásia
        Array<Territorio> orientemedioAdj = new Array<>();
        orientemedioAdj.add(tNomes.get("Egito"));
        orientemedioAdj.add(tNomes.get("Sudão"));
        orientemedioAdj.add(tNomes.get("India"));
        orientemedioAdj.add(tNomes.get("Aral"));
        orientemedioAdj.add(tNomes.get("Polônia"));
        orientemedioAdj.add(tNomes.get("Moscou"));
        mapa.put(tNomes.get("Oriente Médio"), orientemedioAdj);

        Array<Territorio> indiaAdj = new Array<>();
        indiaAdj.add(tNomes.get("Oriente Médio"));
        indiaAdj.add(tNomes.get("Aral"));
        indiaAdj.add(tNomes.get("China"));
        indiaAdj.add(tNomes.get("Vietnã"));
        mapa.put(tNomes.get("India"), indiaAdj);

        Array<Territorio> aralAdj = new Array<>();
        aralAdj.add(tNomes.get("Polônia"));
        aralAdj.add(tNomes.get("Moscou"));
        aralAdj.add(tNomes.get("Omsk"));
        aralAdj.add(tNomes.get("China"));
        aralAdj.add(tNomes.get("Oriente Médio"));
        aralAdj.add(tNomes.get("India"));
        mapa.put(tNomes.get("Aral"), aralAdj);

        Array<Territorio> omskAdj = new Array<>();
        omskAdj.add(tNomes.get("Moscou"));
        omskAdj.add(tNomes.get("Aral"));
        omskAdj.add(tNomes.get("Dudinka"));
        omskAdj.add(tNomes.get("Mongólia"));
        omskAdj.add(tNomes.get("China"));
        mapa.put(tNomes.get("Omsk"), omskAdj);

        Array<Territorio> dudinkaAdj = new Array<>();
        dudinkaAdj.add(tNomes.get("Omsk"));
        dudinkaAdj.add(tNomes.get("Mongólia"));
        dudinkaAdj.add(tNomes.get("Sibéria"));
        dudinkaAdj.add(tNomes.get("Tchita"));
        mapa.put(tNomes.get("Dudinka"), dudinkaAdj);

        Array<Territorio> mongoliaAdj = new Array<>();
        mongoliaAdj.add(tNomes.get("Omsk"));
        mongoliaAdj.add(tNomes.get("Dudinka"));
        mongoliaAdj.add(tNomes.get("Tchita"));
        mongoliaAdj.add(tNomes.get("China"));
        mapa.put(tNomes.get("Mongólia"), mongoliaAdj);

        Array<Territorio> tchitaAdj = new Array<>();
        tchitaAdj.add(tNomes.get("Mongólia"));
        tchitaAdj.add(tNomes.get("Sibéria"));
        tchitaAdj.add(tNomes.get("China"));
        tchitaAdj.add(tNomes.get("Vladvostok"));
        tchitaAdj.add(tNomes.get("Dudinka"));
        mapa.put(tNomes.get("Tchita"), tchitaAdj);

        Array<Territorio> chinaAdj = new Array<>();
        chinaAdj.add(tNomes.get("Aral"));
        chinaAdj.add(tNomes.get("India"));
        chinaAdj.add(tNomes.get("Vietnã"));
        chinaAdj.add(tNomes.get("Omsk"));
        chinaAdj.add(tNomes.get("Mongólia"));
        chinaAdj.add(tNomes.get("Tchita"));
        chinaAdj.add(tNomes.get("Vladvostok"));
        chinaAdj.add(tNomes.get("Japão"));
        mapa.put(tNomes.get("China"), chinaAdj);

        Array<Territorio> vietnaAdj = new Array<>();
        vietnaAdj.add(tNomes.get("India"));
        vietnaAdj.add(tNomes.get("China"));
        vietnaAdj.add(tNomes.get("Sumatra"));
        vietnaAdj.add(tNomes.get("Borneo"));
        mapa.put(tNomes.get("Vietnã"), vietnaAdj);

        Array<Territorio> japaoAdj = new Array<>();
        japaoAdj.add(tNomes.get("Vladvostok"));
        japaoAdj.add(tNomes.get("China"));
        mapa.put(tNomes.get("Japão"), japaoAdj);

        Array<Territorio> vladvostokAdj = new Array<>();
        vladvostokAdj.add(tNomes.get("Tchita"));
        vladvostokAdj.add(tNomes.get("China"));
        vladvostokAdj.add(tNomes.get("Japão"));
        vladvostokAdj.add(tNomes.get("Sibéria"));
        mapa.put(tNomes.get("Vladvostok"), vladvostokAdj);

        Array<Territorio> siberiaAdj = new Array<>();
        siberiaAdj.add(tNomes.get("Dudinka"));
        siberiaAdj.add(tNomes.get("Tchita"));
        siberiaAdj.add(tNomes.get("Vladvostok"));
        mapa.put(tNomes.get("Sibéria"), siberiaAdj);

        // Oceania
        Array<Territorio> australiaAdj = new Array<>();
        australiaAdj.add(tNomes.get("Nova Guiné"));
        australiaAdj.add(tNomes.get("Sumatra"));
        australiaAdj.add(tNomes.get("Borneo"));
        mapa.put(tNomes.get("Australia"), australiaAdj);

        Array<Territorio> novaguineAdj = new Array<>();
        novaguineAdj.add(tNomes.get("Australia"));
        novaguineAdj.add(tNomes.get("Borneo"));
        mapa.put(tNomes.get("Nova Guiné"), novaguineAdj);

        Array<Territorio> sumatraAdj = new Array<>();
        sumatraAdj.add(tNomes.get("Australia"));
        sumatraAdj.add(tNomes.get("Borneo"));
        sumatraAdj.add(tNomes.get("Vietnã"));
        mapa.put(tNomes.get("Sumatra"), sumatraAdj);

        Array<Territorio> borneoAdj = new Array<>();
        borneoAdj.add(tNomes.get("Australia"));
        borneoAdj.add(tNomes.get("Nova Guiné"));
        borneoAdj.add(tNomes.get("Sumatra"));
        borneoAdj.add(tNomes.get("Vietnã"));
        mapa.put(tNomes.get("Borneo"), borneoAdj);

        return mapa;
    }

    public Array<Territorio> getTerritoriosAdj(Territorio territorio){
        return adjacencias.get(territorio);
    }

    public Array<Territorio> getAlidadosAdj(Territorio territorio){
        Array<Territorio> aliados_adj = new Array<>();
        Array<Territorio> adj = adjacencias.get(territorio);
        int tID = territorio.getPlayerId();

        if (adj == null) return aliados_adj;

        for (Territorio t : adj){
            if (t.getPlayerId() == tID){
                aliados_adj.add(t);
            }
        }
        return aliados_adj;
    }

    public Array<Territorio> getInimigosAdj(Territorio territorio){
        Array<Territorio> inimigos_adj = new Array<>();
        Array<Territorio> adj = adjacencias.get(territorio);
        int tID = territorio.getPlayerId();

        if (adj == null) return inimigos_adj;

        for (Territorio t : adj){
            if (t.getPlayerId() != tID){
                inimigos_adj.add(t);
            }
        }
        return inimigos_adj;
    }

    public boolean isAdjacente(Territorio territorioA, Territorio territorioB){
        Array<Territorio> adj = adjacencias.get(territorioA);
        if (adj == null) return false;
        
        for (Territorio t : adj){
            if (t.equals(territorioB)){
                return true;
            }
        }
        return false;
    }

    public Map<Continente, List<Territorio>> getTerritoriosPorContinente() {
        return territoriosPorContinente;
    }

}




