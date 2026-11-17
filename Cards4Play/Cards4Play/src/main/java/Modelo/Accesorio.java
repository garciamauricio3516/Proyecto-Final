package Modelo;

public class Accesorio extends Producto {

    public Accesorio(String nombre, double precioUsd) {
        super(nombre, precioUsd);
    }

    @Override
    public String getTipo() {
        return "ACCESORIO";
    }
}