
package Modelo;

import java.util.Random;

public enum Rareza {

    N("N", 50),
    UC("UC", 25),
    R("R", 15),
    SR("SR", 7),
    UR("UR", 3);

    private final String codigo;
    private final int probabilidad;

    Rareza(String codigo, int probabilidad) {
        this.codigo = codigo;
        this.probabilidad = probabilidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getProbabilidad() {
        return probabilidad;
    }

    public Rareza inferior() {
        int posicion = this.ordinal();

        if (posicion == 0) {
            return null;
        }

        return Rareza.values()[posicion - 1];
    }

    public Rareza superior() {
        int posicion = this.ordinal();

        if (posicion == Rareza.values().length - 1) {
            return null;
        }

        return Rareza.values()[posicion + 1];
    }

    public static Rareza sortear() {
        Random random = new Random();
        int numero = random.nextInt(100);

        int acumulado = 0;

        for (Rareza rareza : Rareza.values()) {
            acumulado += rareza.getProbabilidad();

            if (numero < acumulado) {
                return rareza;
            }
        }

        throw new IllegalStateException(
                "Las probabilidades de rareza no suman 100"
        );
    }
}
