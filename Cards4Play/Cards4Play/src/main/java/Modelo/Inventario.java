
package Modelo;

import java.util.ArrayList;
import java.util.List;

public class Inventario {

    private List<Producto> productos;

    public Inventario() {
        this.productos = new ArrayList<>();
    }

    public void agregarProducto(Producto p) {
        if (p == null) {
            throw new IllegalArgumentException("Producto inválido");
        }

        if (contieneProducto(p.getId())) {
            throw new IllegalStateException("El producto ya existe");
        }

        productos.add(p);
    }

    public Producto retirarProducto(String idProducto) {
        Producto producto = buscarProducto(idProducto);

        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        productos.remove(producto);
        return producto;
    }

    public Producto buscarProducto(String idProducto) {
        for (Producto producto : productos) {
            if (producto.getId().equals(idProducto)) {
                return producto;
            }
        }
        return null;
    }

    public boolean contieneProducto(String idProducto) {
        return buscarProducto(idProducto) != null;
    }

    public List<Producto> listarProductos() {
        return new ArrayList<>(productos);
    }

    public List<Carta> listarCartas() {
        List<Carta> cartas = new ArrayList<>();

        for (Producto producto : productos) {
            if (producto instanceof Carta) {
                cartas.add((Carta) producto);
            }
        }

        return cartas;
    }

    public List<Booster> listarBoostersDisponibles() {
        List<Booster> boosters = new ArrayList<>();

        for (Producto producto : productos) {
            if (producto instanceof Booster) {
                Booster booster = (Booster) producto;

                if (!booster.estaVendido()) {
                    boosters.add(booster);
                }
            }
        }

        return boosters;
    }
}
