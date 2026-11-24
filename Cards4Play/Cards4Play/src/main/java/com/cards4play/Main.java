
package com.cards4play;

import Modelo.Tienda;
import Persistencia.PersistenciaJSON;
import Controladores.*;

public class Main {

    public static void main(String[] args) {

        try {
            // Configurar persistencia
            PersistenciaJSON persistencia =
                    new PersistenciaJSON("datos/tienda.json");

            // Recuperar tienda existente
            Tienda tienda = persistencia.cargar();

            if (tienda == null) {
                tienda = new Tienda("Cards4Play");
                persistencia.guardar(tienda);

                System.out.println("Tienda creada correctamente.");
            } else {
                System.out.println("Tienda recuperada desde JSON.");
            }

            // Inicializar controladores
            ControladorUsuario controladorUsuario =
                    new ControladorUsuario(tienda, persistencia);

            ControladorProducto controladorProducto =
                    new ControladorProducto(tienda, persistencia);

            ControladorCompra controladorCompra =
                    new ControladorCompra(tienda, persistencia);

            ControladorBooster controladorBooster =
                    new ControladorBooster(tienda, persistencia);

            ControladorTorneo controladorTorneo =
                    new ControladorTorneo(tienda, persistencia);

            System.out.println("Bienvenido a " + tienda.getNombre());
            System.out.println("Controladores inicializados correctamente.");

            // Iniciar servidor API REST
            ApiRest apiRest = new ApiRest(
                    tienda,
                    controladorUsuario,
                    controladorProducto,
                    controladorCompra,
                    controladorBooster,
                    controladorTorneo
            );

            apiRest.iniciar();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
