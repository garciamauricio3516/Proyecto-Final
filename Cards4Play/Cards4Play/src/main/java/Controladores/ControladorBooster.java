
package Controladores;

import Modelo.*;
import Persistencia.PersistenciaJSON;

import java.io.IOException;
import java.util.List;

public class ControladorBooster {

    private Tienda tienda;
    private PersistenciaJSON persistencia;

    public ControladorBooster(Tienda tienda, PersistenciaJSON persistencia) {
        this.tienda = tienda;
        this.persistencia = persistencia;
    }

    public List<Carta> abrirBooster(String idCliente, String idBooster) {

        List<Carta> cartas = tienda.abrirBooster(idCliente, idBooster);

        guardarCambios();

        return cartas;
    }

    public List<Booster> listarBoostersDisponibles() {
        return tienda.getInventario().listarBoostersDisponibles();
    }

    public boolean estaAbierto(String idCliente, String idBooster) {
        Booster booster = buscarBoosterCliente(idCliente, idBooster);
        return booster.estaAbierto();
    }

    public List<Carta> consultarCartas(String idCliente, String idBooster) {
        Booster booster = buscarBoosterCliente(idCliente, idBooster);
        return booster.getCartasSiAbierto();
    }

    private Booster buscarBoosterCliente(String idCliente, String idBooster) {
        Cliente cliente = tienda.buscarCliente(idCliente);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        Producto producto = cliente.getInventarioPersonal()
                .buscarProducto(idBooster);

        if (!(producto instanceof Booster booster)) {
            throw new IllegalArgumentException("Booster no encontrado");
        }

        return booster;
    }

    private void guardarCambios() {

        try {
            persistencia.guardar(tienda);
            System.out.println("Booster guardado correctamente en JSON.");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "El booster se abrió, pero no se pudo guardar en JSON.",
                    e
            );
        }
    }
}


