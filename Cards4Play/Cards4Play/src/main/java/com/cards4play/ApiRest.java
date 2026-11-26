package com.cards4play;

import Controladores.ControladorUsuario;
import Modelo.Usuario;
import Modelo.Tienda;
import Modelo.Administrador;
import Modelo.Cliente;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;

import com.google.gson.Gson;
import spark.Request;

import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import static spark.Spark.*;

import Controladores.ControladorProducto;
import Modelo.Producto;
import Modelo.Carta;
import Modelo.Rareza;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import Controladores.ControladorCompra;
import Controladores.ControladorBooster;
import Modelo.Compra;
import Modelo.Booster;

import Controladores.ControladorTorneo;
import Modelo.Torneo;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ApiRest {

    private final Gson gson = new Gson();
    private final Tienda tienda;
    private final ControladorUsuario controladorUsuario;
    private final ControladorProducto controladorProducto;
    private final ControladorCompra controladorCompra;
    private final ControladorBooster controladorBooster;
    private final ControladorTorneo controladorTorneo;

    public ApiRest(
            Tienda tienda,
            ControladorUsuario controladorUsuario,
            ControladorProducto controladorProducto,
            ControladorCompra controladorCompra,
            ControladorBooster controladorBooster,
            ControladorTorneo controladorTorneo) {

        this.tienda = tienda;
        this.controladorUsuario = controladorUsuario;
        this.controladorProducto = controladorProducto;
        this.controladorCompra = controladorCompra;
        this.controladorBooster = controladorBooster;
        this.controladorTorneo = controladorTorneo;
    }

    public void iniciar() {

        String puertoEntorno = System.getenv("PORT");

        int puerto = puertoEntorno == null
                ? 4567
                : Integer.parseInt(puertoEntorno);

        ipAddress("0.0.0.0");
        port(puerto);

        // Configurar respuestas JSON y autenticación
        before((request, response) -> {

            response.type("application/json; charset=UTF-8");

            // Ruta pública para comprobar el servidor
            if (request.pathInfo().equals("/health")
                    && request.requestMethod().equals("GET")) {
                return;
            }

            Usuario usuario = autenticar(request);

            if (usuario == null) {

                response.header(
                        "WWW-Authenticate",
                        "Basic realm=\"Cards4Play\""
                );

                halt(401, error("Credenciales inválidas o ausentes"));
            }

            request.attribute("usuario", usuario);
        });

        // Comprobar estado del servidor
        get("/health", (request, response) -> {

            response.status(200);

            return gson.toJson(Map.of(
                    "estado", "OK",
                    "aplicacion", "Cards4Play"
            ));
        });

        // Consultar usuario autenticado
        get("/usuarios/me", (request, response) -> {

            Usuario usuario = request.attribute("usuario");

            response.status(200);

            return gson.toJson(Map.of(
                    "id", usuario.getId(),
                    "nombreUsuario", usuario.getNombreUsuario(),
                    "rol", usuario.getRol().name()
            ));
        });

        // Registrar un nuevo cliente
        post("/usuarios", (request, response) -> {

            Administrador admin =
                    (Administrador) exigirAdministrador(request);

            try {

                JsonObject datos = gson.fromJson(
                        request.body(),
                        JsonObject.class
                );

                if (datos == null
                        || !datos.has("nombreUsuario")
                        || !datos.has("contrasena")
                        || !datos.get("nombreUsuario").isJsonPrimitive()
                        || !datos.get("contrasena").isJsonPrimitive()
                        || !datos.get("nombreUsuario").getAsJsonPrimitive().isString()
                        || !datos.get("contrasena").getAsJsonPrimitive().isString()) {

                    response.status(400);
                    return error("Nombre de usuario y contraseña obligatorios");
                }

                String nombreUsuario =
                        datos.get("nombreUsuario").getAsString();

                String contrasena =
                        datos.get("contrasena").getAsString();

                Cliente cliente;

                synchronized (tienda) {

                    cliente = controladorUsuario.registrarCliente(
                            admin,
                            nombreUsuario,
                            contrasena
                    );
                }

                response.status(201);

                return gson.toJson(Map.of(
                        "id", cliente.getId(),
                        "nombreUsuario", cliente.getNombreUsuario(),
                        "rol", cliente.getRol().name(),
                        "mensaje", "Cliente registrado correctamente"
                ));

            } catch (JsonParseException | IllegalArgumentException e) {

                response.status(400);
                return error("Datos inválidos");

            } catch (SecurityException e) {

                response.status(403);
                return error(e.getMessage());

            } catch (IllegalStateException e) {

                if (e.getCause() instanceof IOException) {

                    response.status(500);
                    return error("Error al guardar los datos");
                }

                response.status(409);
                return error(e.getMessage());
            }
        });

        // Listar productos del inventario
        get("/productos", (request, response) -> {

            synchronized (tienda) {

                double tasa = tienda.obtenerTasaCambio();
                String fuente = tienda.getUltimaFuenteTasa();

                List<Map<String, Object>> productos = new ArrayList<>();

                for (Producto producto :
                        controladorProducto.listarProductos()) {

                    productos.add(
                            productoJson(producto, tasa, fuente)
                    );
                }

                response.status(200);
                return gson.toJson(productos);
            }
        });

// Consultar un producto por ID
        get("/productos/:id", (request, response) -> {

            synchronized (tienda) {

                Producto producto = controladorProducto.buscarProducto(
                        request.params(":id")
                );

                if (producto == null) {
                    response.status(404);
                    return error("Producto no encontrado");
                }

                double tasa = tienda.obtenerTasaCambio();

                response.status(200);

                return gson.toJson(productoJson(
                        producto,
                        tasa,
                        tienda.getUltimaFuenteTasa()
                ));
            }
        });

// Registrar cartas
        post("/cartas", (request, response) ->
                registrarProducto(request, response, "CARTA")
        );

// Registrar accesorios
        post("/accesorios", (request, response) ->
                registrarProducto(request, response, "ACCESORIO")
        );

// Registrar productos sellados
        post("/productos-sellados", (request, response) ->
                registrarProducto(request, response, "SELLADO")
        );

// Crear boosters
        post("/boosters", (request, response) ->
                registrarProducto(request, response, "BOOSTER")
        );

        // Comprar un producto
        post("/compras", (request, response) -> {

            Usuario cliente = exigirCliente(request);

            try {

                JsonObject datos = gson.fromJson(
                        request.body(),
                        JsonObject.class
                );

                if (datos == null) {
                    throw new IllegalArgumentException(
                            "El cuerpo JSON es obligatorio"
                    );
                }

                String idProducto = leerTexto(datos, "idProducto");

                synchronized (tienda) {

                    Compra compra = controladorCompra.comprarProducto(
                            cliente.getId(),
                            idProducto
                    );

                    response.status(201);

                    return gson.toJson(compraJson(compra));
                }

            } catch (JsonParseException e) {

                response.status(400);
                return error("JSON inválido");

            } catch (IllegalArgumentException e) {

                String mensaje = e.getMessage();

                response.status(
                        mensaje != null
                                && mensaje.toLowerCase().contains("no encontrado")
                                ? 404 : 400
                );

                return error(mensaje);

            } catch (SecurityException e) {

                response.status(403);
                return error(e.getMessage());

            } catch (IllegalStateException e) {

                if (e.getCause() instanceof IOException) {
                    response.status(500);
                    return error("Error al guardar la compra");
                }

                response.status(409);
                return error(e.getMessage());
            }
        });

// Consultar historial de compras propio
        get("/compras", (request, response) -> {

            Usuario cliente = exigirCliente(request);

            synchronized (tienda) {

                List<Map<String, Object>> historial = new ArrayList<>();

                for (Compra compra :
                        controladorCompra.historialDeCliente(
                                cliente.getId())) {

                    historial.add(compraJson(compra));
                }

                response.status(200);

                return gson.toJson(historial);
            }
        });

// Abrir un booster comprado
        post("/boosters/:id/abrir", (request, response) -> {

            Usuario cliente = exigirCliente(request);

            String idBooster = request.params(":id");

            try {

                synchronized (tienda) {

                    List<Carta> cartas = controladorBooster.abrirBooster(
                            cliente.getId(),
                            idBooster
                    );

                    double tasa = tienda.obtenerTasaCambio();
                    String fuente = tienda.getUltimaFuenteTasa();

                    List<Map<String, Object>> contenido = new ArrayList<>();

                    for (Carta carta : cartas) {

                        contenido.add(
                                productoJson(carta, tasa, fuente)
                        );
                    }

                    response.status(200);

                    return gson.toJson(Map.of(
                            "idBooster", idBooster,
                            "mensaje", "Booster abierto correctamente",
                            "cantidadCartas", cartas.size(),
                            "cartas", contenido
                    ));
                }

            } catch (IllegalArgumentException e) {

                response.status(404);
                return error(e.getMessage());

            } catch (SecurityException e) {

                response.status(403);
                return error(e.getMessage());

            } catch (IllegalStateException e) {

                if (e.getCause() instanceof IOException) {
                    response.status(500);
                    return error("Error al guardar la apertura");
                }

                response.status(409);
                return error(e.getMessage());
            }
        });

        // Crear un torneo
        post("/torneos", (request, response) -> {

            Administrador admin =
                    (Administrador) exigirAdministrador(request);

            try {

                JsonObject datos = gson.fromJson(
                        request.body(),
                        JsonObject.class
                );

                if (datos == null) {
                    throw new IllegalArgumentException(
                            "El cuerpo JSON es obligatorio"
                    );
                }

                String nombre = leerTexto(datos, "nombre");

                LocalDate fecha = LocalDate.parse(
                        leerTexto(datos, "fecha")
                );

                if (!datos.has("maxParticipantes")
                        || !datos.get("maxParticipantes").isJsonPrimitive()
                        || !datos.get("maxParticipantes")
                        .getAsJsonPrimitive().isNumber()) {

                    throw new IllegalArgumentException(
                            "El cupo máximo es obligatorio"
                    );
                }

                int maxParticipantes = datos.get("maxParticipantes")
                        .getAsBigDecimal().intValueExact();

                if (maxParticipantes <= 0) {
                    throw new IllegalArgumentException(
                            "El cupo debe ser mayor a cero"
                    );
                }

                synchronized (tienda) {

                    Torneo torneo = controladorTorneo.crearTorneo(
                            admin,
                            nombre,
                            fecha,
                            maxParticipantes
                    );

                    response.status(201);

                    return gson.toJson(torneoJson(torneo));
                }

            } catch (JsonParseException | IllegalArgumentException
                     | DateTimeParseException | ArithmeticException e) {

                response.status(400);
                return error("Datos del torneo inválidos");

            } catch (SecurityException e) {

                response.status(403);
                return error(e.getMessage());

            } catch (IllegalStateException e) {

                if (e.getCause() instanceof IOException) {
                    response.status(500);
                    return error("Error al guardar el torneo");
                }

                response.status(409);
                return error(e.getMessage());
            }
        });

// Listar todos los torneos
        get("/torneos", (request, response) -> {

            synchronized (tienda) {

                List<Map<String, Object>> torneos = new ArrayList<>();

                for (Torneo torneo :
                        controladorTorneo.listarTorneos()) {

                    torneos.add(torneoJson(torneo));
                }

                response.status(200);

                return gson.toJson(torneos);
            }
        });

// Consultar un torneo por ID
        get("/torneos/:id", (request, response) -> {

            synchronized (tienda) {

                Torneo torneo = controladorTorneo.buscarTorneo(
                        request.params(":id")
                );

                if (torneo == null) {

                    response.status(404);
                    return error("Torneo no encontrado");
                }

                response.status(200);

                return gson.toJson(torneoJson(torneo));
            }
        });

// Inscribir al cliente autenticado
        post("/torneos/:id/inscripciones", (request, response) -> {

            Usuario cliente = exigirCliente(request);

            String idTorneo = request.params(":id");

            try {

                synchronized (tienda) {

                    Torneo torneo =
                            controladorTorneo.buscarTorneo(idTorneo);

                    if (torneo == null) {

                        response.status(404);
                        return error("Torneo no encontrado");
                    }

                    controladorTorneo.inscribirEnTorneo(
                            idTorneo,
                            cliente.getId()
                    );

                    response.status(201);

                    return gson.toJson(Map.of(
                            "mensaje", "Inscripción realizada correctamente",
                            "idCliente", cliente.getId(),
                            "torneo", torneoJson(torneo)
                    ));
                }

            } catch (IllegalArgumentException e) {

                response.status(400);
                return error(e.getMessage());

            } catch (SecurityException e) {

                response.status(403);
                return error(e.getMessage());

            } catch (IllegalStateException e) {

                if (e.getCause() instanceof IOException) {
                    response.status(500);
                    return error("Error al guardar la inscripción");
                }

                response.status(409);
                return error(e.getMessage());
            }
        });

        // Manejar rutas inexistentes
        notFound((request, response) -> {

            response.type("application/json; charset=UTF-8");

            return error("Ruta no encontrada");
        });

        awaitInitialization();

        System.out.println(
                "API REST iniciada en el puerto " + puerto
        );
    }

    // Validar credenciales HTTP Basic
    private Usuario autenticar(Request request) {

        String autorizacion = request.headers("Authorization");

        if (autorizacion == null
                || !autorizacion.regionMatches(
                true, 0, "Basic ", 0, 6)) {
            return null;
        }

        try {

            String credenciales = new String(
                    Base64.getDecoder().decode(
                            autorizacion.substring(6).trim()
                    ),
                    StandardCharsets.UTF_8
            );

            String[] partes = credenciales.split(":", 2);

            if (partes.length != 2) {
                return null;
            }

            synchronized (tienda) {
                return controladorUsuario.autenticar(
                        partes[0],
                        partes[1]
                );
            }

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Verificar permisos de administrador
    private Usuario exigirAdministrador(Request request) {

        Usuario usuario = request.attribute("usuario");

        if (usuario == null) {
            halt(401, error("Usuario no autenticado"));
        }

        if (!usuario.esAdmin()) {
            halt(403, error("Se requieren permisos de administrador"));
        }

        return usuario;
    }

    // Verificar permisos de cliente
    private Usuario exigirCliente(Request request) {

        Usuario usuario = request.attribute("usuario");

        if (usuario == null) {
            halt(401, error("Usuario no autenticado"));
        }

        if (!(usuario instanceof Modelo.Cliente)) {
            halt(403, error("Se requieren permisos de cliente"));
        }

        return usuario;
    }

    // Registrar productos según su tipo
    private String registrarProducto(
            Request request,
            spark.Response response,
            String tipo) {

        Administrador admin =
                (Administrador) exigirAdministrador(request);

        try {

            JsonObject datos = gson.fromJson(
                    request.body(),
                    JsonObject.class
            );

            if (datos == null) {
                throw new IllegalArgumentException(
                        "El cuerpo JSON es obligatorio"
                );
            }

            double precioUsd = leerPrecio(datos);

            synchronized (tienda) {

                Producto producto;

                switch (tipo) {

                    case "CARTA":
                        producto = controladorProducto.registrarCarta(
                                admin,
                                leerTexto(datos, "nombre"),
                                Rareza.valueOf(
                                        leerTexto(datos, "rareza")
                                                .toUpperCase(java.util.Locale.ROOT)
                                ),
                                precioUsd
                        );
                        break;

                    case "ACCESORIO":
                        producto = controladorProducto.registrarAccesorio(
                                admin,
                                leerTexto(datos, "nombre"),
                                precioUsd
                        );
                        break;

                    case "SELLADO":
                        producto = controladorProducto.registrarProductoSellado(
                                admin,
                                leerTexto(datos, "nombre"),
                                precioUsd
                        );
                        break;

                    case "BOOSTER":
                        producto = controladorProducto.crearBooster(
                                admin,
                                precioUsd
                        );
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Tipo de producto inválido"
                        );
                }

                double tasa = tienda.obtenerTasaCambio();

                response.status(201);

                return gson.toJson(productoJson(
                        producto,
                        tasa,
                        tienda.getUltimaFuenteTasa()
                ));
            }

        } catch (JsonParseException | IllegalArgumentException e) {

            response.status(400);
            return error("Datos del producto inválidos");

        } catch (SecurityException e) {

            response.status(403);
            return error(e.getMessage());

        } catch (IllegalStateException e) {

            if (e.getCause() instanceof IOException) {
                response.status(500);
                return error("Error al guardar los datos");
            }

            response.status(409);
            return error(e.getMessage());
        }
    }

    // Validar campos de texto
    private String leerTexto(JsonObject datos, String campo) {

        if (!datos.has(campo)
                || !datos.get(campo).isJsonPrimitive()
                || !datos.get(campo).getAsJsonPrimitive().isString()) {

            throw new IllegalArgumentException(
                    "Campo obligatorio: " + campo
            );
        }

        String valor = datos.get(campo).getAsString().trim();

        if (valor.isEmpty()) {
            throw new IllegalArgumentException(
                    "Campo vacío: " + campo
            );
        }

        return valor;
    }

    // Validar el precio
    private double leerPrecio(JsonObject datos) {

        if (!datos.has("precioUsd")
                || !datos.get("precioUsd").isJsonPrimitive()
                || !datos.get("precioUsd").getAsJsonPrimitive().isNumber()) {

            throw new IllegalArgumentException(
                    "El precio es obligatorio y debe ser numérico"
            );
        }

        double precio = datos.get("precioUsd").getAsDouble();

        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("Precio inválido");
        }

        return precio;
    }

    // Construir una respuesta segura del producto
    private Map<String, Object> productoJson(
            Producto producto,
            double tasa,
            String fuente) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", producto.getId());
        datos.put("nombre", producto.getNombre());
        datos.put("tipo", producto.getTipo());
        datos.put("precioUsd", producto.getPrecioUsd());
        datos.put("precioCop", producto.calcularPrecioCop(tasa));
        datos.put("tasaCambio", tasa);
        datos.put("fuenteTasa", fuente);

        if (producto instanceof Carta carta) {
            datos.put("rareza", carta.getRareza().name());
        }

        return datos;
    }

    // Construir una respuesta segura de la compra

    // Construir una respuesta segura de la compra
    private Map<String, Object> compraJson(Compra compra) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", compra.getId());
        datos.put("idCliente", compra.getIdCliente());
        datos.put("fecha", compra.getFecha().toString());
        datos.put("totalUsd", compra.getTotalUsd());
        datos.put("totalCop", compra.getTotalCop());
        datos.put("fuenteTasa", compra.getFuenteTasa());

        List<Map<String, Object>> productos = new ArrayList<>();

        for (Producto producto : compra.getProductos()) {

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("id", producto.getId());
            item.put("nombre", producto.getNombre());
            item.put("tipo", producto.getTipo());
            item.put("precioUsd", producto.getPrecioUsd());

            if (producto instanceof Carta carta) {
                item.put("rareza", carta.getRareza().name());
            }

            if (producto instanceof Booster booster) {

                item.put("abierto", booster.estaAbierto());

                if (booster.estaAbierto()) {

                    List<Map<String, Object>> contenido =
                            new ArrayList<>();

                    for (Carta carta : booster.getCartasSiAbierto()) {

                        Map<String, Object> cartaDatos =
                                new LinkedHashMap<>();

                        cartaDatos.put("id", carta.getId());
                        cartaDatos.put("nombre", carta.getNombre());
                        cartaDatos.put("tipo", carta.getTipo());
                        cartaDatos.put("rareza", carta.getRareza().name());
                        cartaDatos.put("precioUsd", carta.getPrecioUsd());

                        contenido.add(cartaDatos);
                    }

                    item.put("cantidadCartas", contenido.size());
                    item.put("cartas", contenido);
                }
            }

            productos.add(item);
        }

        datos.put("productos", productos);

        return datos;
    }


    // Construir una respuesta JSON del torneo
    private Map<String, Object> torneoJson(Torneo torneo) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", torneo.getId());
        datos.put("nombre", torneo.getNombre());
        datos.put("fecha", torneo.getFecha().toString());
        datos.put("maxParticipantes", torneo.getMaxParticipantes());
        datos.put("participantesInscritos",
                torneo.getParticipantes().size());
        datos.put("cuposDisponibles", torneo.getCuposDisponibles());

        return datos;
    }

    // Generar errores en formato JSON
    private String error(String mensaje) {

        return gson.toJson(Map.of(
                "error", mensaje
        ));
    }
}