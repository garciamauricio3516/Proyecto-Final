
package Controladores;

import Modelo.*;
import Persistencia.PersistenciaJSON;

import java.io.IOException;
import java.util.List;

public class ControladorProducto {

    private Tienda tienda;
    private PersistenciaJSON persistencia;

    public ControladorProducto(Tienda tienda, PersistenciaJSON persistencia) {
        this.tienda = tienda;
        this.persistencia = persistencia;
    }

    public Carta registrarCarta(Administrador admin, String nombre,
                                Rareza rareza, double precioUsd) {
        validarAdmin(admin);

        Carta carta = admin.registrarCarta(nombre, rareza, precioUsd);
        tienda.getInventario().agregarProducto(carta);

        guardarCambios();
        return carta;
    }

    public Booster crearBooster(Administrador admin, double precioUsd) {
        validarAdmin(admin);

        List<Carta> cartas = tienda.getInventario().listarCartas();

        Booster booster = admin.crearBooster(precioUsd, cartas);
        tienda.getInventario().agregarProducto(booster);

        guardarCambios();
        return booster;
    }

    public ProductoSellado registrarProductoSellado(Administrador admin,
                                                    String nombre, double precioUsd) {
        validarAdmin(admin);

        ProductoSellado producto = admin.registrarProductoSellado(nombre, precioUsd);
        tienda.getInventario().agregarProducto(producto);

        guardarCambios();
        return producto;
    }

    public Accesorio registrarAccesorio(Administrador admin,
                                        String nombre, double precioUsd) {
        validarAdmin(admin);

        Accesorio accesorio = admin.registrarAccesorio(nombre, precioUsd);
        tienda.getInventario().agregarProducto(accesorio);

        guardarCambios();
        return accesorio;
    }

    public List<Producto> listarProductos() {
        return tienda.getInventario().listarProductos();
    }

    public Producto buscarProducto(String idProducto) {
        return tienda.getInventario().buscarProducto(idProducto);
    }

    private void validarAdmin(Administrador admin) {
        if (!tienda.esAdministradorRegistrado(admin)) {
            throw new SecurityException("Administrador no autorizado");
        }
    }

    private void guardarCambios() {
        try {
            persistencia.guardar(tienda);
            System.out.println("Productos guardados correctamente en JSON.");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Se modificó el inventario, pero no se pudo guardar en JSON.",
                    e
            );
        }
    }
}

