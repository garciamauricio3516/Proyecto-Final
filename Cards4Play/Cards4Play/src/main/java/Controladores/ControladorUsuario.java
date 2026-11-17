
package Controladores;

import Modelo.Administrador;
import Modelo.Cliente;
import Modelo.Tienda;
import Modelo.Usuario;
import Persistencia.PersistenciaJSON;

import java.io.IOException;

public class ControladorUsuario {

    private Tienda tienda;
    private PersistenciaJSON persistencia;

    public ControladorUsuario(Tienda tienda, PersistenciaJSON persistencia) {
        this.tienda = tienda;
        this.persistencia = persistencia;
    }

    public Cliente registrarCliente(Administrador admin, String user, String pass) {

        Cliente cliente = tienda.registrarCliente(admin, user, pass);

        try {
            persistencia.guardar(tienda);
            System.out.println("Cliente guardado correctamente en JSON.");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "El cliente se registró, pero no se pudo guardar en JSON.",
                    e
            );
        }

        return cliente;
    }

    public Usuario autenticar(String user, String pass) {
        return tienda.autenticar(user, pass);
    }

    public Cliente buscarCliente(String idCliente) {
        return tienda.buscarCliente(idCliente);
    }
}


