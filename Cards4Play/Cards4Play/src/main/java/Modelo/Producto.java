package Modelo;

import java.util.UUID;

public abstract class Producto {

    private String id;
    private String nombre;
    private double precioUsd;

    public Producto(String nombre, double precioUsd) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        setPrecioUsd(precioUsd);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioUsd() {
        return precioUsd;
    }

    public void setPrecioUsd(double precioUsd) {
        if (!Double.isFinite(precioUsd) || precioUsd < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo ni inválido");
        }
        this.precioUsd = precioUsd;
    }

    public double calcularPrecioCop(double tasa) {
        if (!Double.isFinite(tasa) || tasa <= 0) {
            throw new IllegalArgumentException("La tasa de cambio debe ser positiva");
        }
        return precioUsd * tasa;
    }

    public abstract String getTipo();
}