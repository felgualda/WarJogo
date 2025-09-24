package com.gruposete.war;

public class Utils {

    public static float[] multiplicarPontos(float[] pontos, float fat) {
        float[] resp = new float[pontos.length];
        for(int i = 0; i < pontos.length; i++){
            resp[i] = pontos[i] * fat;
        }

        for(int i = 0; i < pontos.length; i += 2){
            resp[i] -= 100;
            resp[i + 1] -= 20;
        }
        return resp;
    }
}
