
package Controladores;

import Modelo.Compra;
import Modelo.Tienda;
import Persistencia.PersistenciaJSON;

import java.io.IOException;
import java.util.List;

public class ControladorCompra {

    private Tienda tienda;
    private PersistenciaJSON persistencia;

    public ControladorCompra(Tienda tienda, PersistenciaJSON persistencia) {
        this.tienda = tienda;
        this.persistencia = persistencia;
    }

    public Compra comprarProducto(String idCliente, String idProducto) {

        Compra compra = tienda.comprarProducto(idCliente, idProducto);

        guardarCambios();

        return compra;
    }

    public List<Compra> historialDeCliente(String idCliente) {
        return tienda.historialDeCliente(idCliente);
    }

    public double obtenerTasaCambio() {
        return tienda.obtenerTasaCambio();
    }

    private void guardarCambios() {

        try {
            persistencia.guardar(tienda);

            System.out.println("Compra guardada correctamente en JSON.");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "La compra se realizó, pero no se pudo guardar en JSON.",
                    e
            );
        }
    }
}

