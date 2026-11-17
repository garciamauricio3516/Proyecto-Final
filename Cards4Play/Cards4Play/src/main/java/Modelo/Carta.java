package Modelo;

public class Carta extends Producto {

    private Rareza rareza;

    public Carta(String nombre, Rareza rareza, double precioUsd) {
        super(nombre, precioUsd);
        this.rareza = rareza;
    }

    public Rareza getRareza() {
        return rareza;
    }

    @Override
    public String getTipo() {
        return "CARTA";
    }
}