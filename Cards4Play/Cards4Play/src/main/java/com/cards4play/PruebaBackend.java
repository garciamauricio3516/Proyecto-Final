
package com.cards4play;

import Modelo.*;
import Persistencia.PersistenciaJSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class PruebaBackend {

    private static int pruebas = 0;

    public static void main(String[] args) throws Exception {

        Path carpeta = Files.createTempDirectory("cards4play-pruebas-");
        Path archivo = carpeta.resolve("tienda.json");

        PersistenciaJSON persistencia =
                new PersistenciaJSON(archivo.toString());

        Tienda tienda = new Tienda("Cards4Play Pruebas");

        System.out.println("\n=== PRUEBAS CARDS4PLAY ===\n");

        // 1. Autenticacion.

        Administrador admin = (Administrador)
                tienda.autenticar("admin", "admin123");

        verificar(admin != null, "Autenticacion del administrador");

        verificar(
                tienda.autenticar("admin", "incorrecta") == null,
                "Rechazo de contrasena incorrecta"
        );

        // 2. Registro de clientes.

        Cliente cliente1 = tienda.registrarCliente(
                admin, "cliente1", "prueba123"
        );

        Cliente cliente2 = tienda.registrarCliente(
                admin, "cliente2", "prueba456"
        );

        verificar(
                tienda.autenticar("cliente1", "prueba123") != null,
                "Registro y autenticacion del cliente"
        );

        // 3. Registro de cartas.

        for (Rareza rareza : Rareza.values()) {

            for (int i = 1; i <= 3; i++) {

                Carta carta = new Carta(
                        "Carta " + rareza.name() + " " + i,
                        rareza,
                        5.0
                );

                tienda.getInventario().agregarProducto(carta);
            }
        }

        verificar(
                tienda.getInventario().listarCartas().size() == 15,
                "Registro de 15 cartas"
        );

        // 4. Generacion de booster.

        Booster booster = new Booster(10.0);

        booster.generarContenido(
                tienda.getInventario().listarCartas()
        );

        tienda.getInventario().agregarProducto(booster);

        verificar(
                booster.getCartasSiAbierto().isEmpty(),
                "Contenido oculto antes de abrir"
        );

        verificar(
                tienda.getInventario().listarCartas().size() == 15,
                "Las cartas originales permanecen en el inventario"
        );

        // 5. Compra y conversion de moneda.

        Compra compra = tienda.comprarProducto(
                cliente1.getId(),
                booster.getId()
        );

        verificar(
                booster.estaVendido(),
                "Compra del booster"
        );

        verificar(
                compra.getTotalCop() > 0,
                "Conversion de USD a COP"
        );

        verificar(
                "API".equals(compra.getFuenteTasa())
                        || "DEFECTO".equals(compra.getFuenteTasa()),
                "Identificacion de la fuente de conversion"
        );

        System.out.println("USD: " + compra.getTotalUsd());
        System.out.println("COP: " + compra.getTotalCop());
        System.out.println("Fuente: " + compra.getFuenteTasa());

        verificarError(
                IllegalStateException.class,
                () -> tienda.comprarProducto(
                        cliente2.getId(), booster.getId()
                ),
                "Rechazo de segunda compra"
        );

        // 6. Restricciones de apertura.

        verificarError(
                SecurityException.class,
                () -> tienda.abrirBooster(
                        cliente2.getId(), booster.getId()
                ),
                "Rechazo de apertura por otro cliente"
        );

        List<Carta> cartas = tienda.abrirBooster(
                cliente1.getId(),
                booster.getId()
        );

        verificar(
                cartas.size() == 10,
                "El booster contiene exactamente 10 cartas"
        );

        verificar(
                cartas.stream().anyMatch(carta ->
                        carta.getRareza().ordinal()
                                >= Rareza.R.ordinal()),
                "Garantia de una carta R o superior"
        );

        verificarError(
                IllegalStateException.class,
                () -> tienda.abrirBooster(
                        cliente1.getId(), booster.getId()
                ),
                "Rechazo de segunda apertura"
        );

        // 7. Torneos.

        Torneo torneo = new Torneo(
                "Torneo de prueba",
                LocalDate.now().plusDays(7),
                1
        );

        tienda.agregarTorneo(torneo);

        tienda.inscribirEnTorneo(
                torneo.getId(), cliente1.getId()
        );

        verificar(
                torneo.estaInscrito(cliente1.getId()),
                "Inscripcion en torneo"
        );

        verificarError(
                IllegalStateException.class,
                () -> tienda.inscribirEnTorneo(
                        torneo.getId(), cliente1.getId()
                ),
                "Rechazo de inscripcion duplicada"
        );

        verificarError(
                IllegalStateException.class,
                () -> tienda.inscribirEnTorneo(
                        torneo.getId(), cliente2.getId()
                ),
                "Rechazo de inscripcion en torneo lleno"
        );

        // 8. Persistencia JSON.

        persistencia.guardar(tienda);

        verificar(
                Files.exists(archivo),
                "Creacion del archivo JSON"
        );

        Tienda recuperada = persistencia.cargar();

        verificar(
                recuperada != null,
                "Recuperacion de la tienda"
        );

        verificar(
                recuperada.autenticar(
                        "cliente1", "prueba123"
                ) != null,
                "Persistencia de usuarios"
        );

        verificar(
                recuperada.historialDeCliente(
                        cliente1.getId()
                ).size() == 1,
                "Persistencia del historial de compras"
        );

        verificar(
                recuperada.listarTorneos().size() == 1,
                "Persistencia de torneos"
        );

        verificarError(
                IllegalStateException.class,
                () -> recuperada.abrirBooster(
                        cliente1.getId(), booster.getId()
                ),
                "Persistencia del estado del booster abierto"
        );

        System.out.println("\n============================");
        System.out.println("PRUEBAS SUPERADAS: " + pruebas);
        System.out.println("TODAS LAS PRUEBAS CORRECTAS");
        System.out.println("============================");

        Files.deleteIfExists(archivo);
        Files.deleteIfExists(carpeta);
    }

    private static void verificar(
            boolean condicion,
            String descripcion) {

        if (!condicion) {
            throw new AssertionError("ERROR: " + descripcion);
        }

        pruebas++;

        System.out.println("[OK] " + descripcion);
    }

    private static void verificarError(
            Class<? extends RuntimeException> tipo,
            Runnable accion,
            String descripcion) {

        try {

            accion.run();

        } catch (RuntimeException e) {

            verificar(tipo.isInstance(e), descripcion);
            return;
        }

        throw new AssertionError(
                "No se produjo el error esperado: " + descripcion
        );
    }
}
