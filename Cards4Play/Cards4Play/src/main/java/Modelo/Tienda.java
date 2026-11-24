
package Modelo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Tienda {

    private String nombre;
    private Inventario inventario;
    private List<Usuario> usuarios;
    private List<Compra> compras;
    private List<Torneo> torneos;
    private double tasaCambioDefecto;
    private String ultimaFuenteTasa = "DEFECTO";


    public Tienda(String nombre) {

        this.nombre = nombre;
        this.inventario = new Inventario();
        this.usuarios = new ArrayList<>();
        this.compras = new ArrayList<>();
        this.torneos = new ArrayList<>();

        this.tasaCambioDefecto = leerTasaDefecto();

        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (adminPassword == null || adminPassword.isBlank()) {
            if (System.getenv("RAILWAY_ENVIRONMENT") != null) {
                throw new IllegalStateException(
                        "Debe configurar ADMIN_PASSWORD en Railway");
            }

            adminPassword = "admin123";
        }

        usuarios.add(new Administrador("admin", adminPassword));
    }



    public Cliente registrarCliente(Administrador admin, String user, String pass) {

        if (!esAdministradorRegistrado(admin)) {
            throw new SecurityException("Administrador no autorizado");
        }

        if (user == null || user.isBlank() ||
                pass == null || pass.isBlank()) {
            throw new IllegalArgumentException("Datos inválidos");
        }

        for (Usuario usuario : usuarios) {
            if (usuario.getNombreUsuario().equals(user)) {
                throw new IllegalStateException("El usuario ya existe");
            }
        }

        Cliente cliente = new Cliente(user, pass);
        usuarios.add(cliente);

        return cliente;
    }


    public Usuario autenticar(String user, String pass) {
        for (Usuario usuario : usuarios) {
            if (usuario.autenticar(user, pass)) {
                return usuario;
            }
        }
        return null;
    }

    public Cliente buscarCliente(String idCliente) {
        for (Usuario usuario : usuarios) {
            if (usuario instanceof Cliente cliente &&
                    cliente.getId().equals(idCliente)) {
                return cliente;
            }
        }
        return null;
    }

    private double leerTasaDefecto() {

        String valor = System.getenv("TASA_DEFECTO");

        if (valor != null && !valor.isBlank()) {
            try {
                double tasa = Double.parseDouble(valor.trim());

                if (Double.isFinite(tasa) && tasa > 0) {
                    return tasa;
                }

            } catch (NumberFormatException e) {
                // Se utiliza el valor predeterminado.
            }
        }

        return 4000.0;
    }

    public double obtenerTasaCambio() {
        return consultarTasaCambio();
    }

    public String getUltimaFuenteTasa() {
        return ultimaFuenteTasa;
    }

    private double consultarTasaCambio() {

        this.tasaCambioDefecto = leerTasaDefecto();

        try {
            HttpClient cliente = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://open.er-api.com/v6/latest/USD"
                    ))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> respuesta = cliente.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (respuesta.statusCode() == 200) {

                JsonObject json = JsonParser
                        .parseString(respuesta.body())
                        .getAsJsonObject();

                JsonObject tasas = json.getAsJsonObject("rates");

                if (tasas != null && tasas.has("COP")) {

                    double tasa = tasas.get("COP").getAsDouble();

                    if (Double.isFinite(tasa) && tasa > 0) {
                        ultimaFuenteTasa = "API";
                        return tasa;
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            // Se utiliza la tasa por defecto.
        }

        System.out.println(
                "La API no respondió, por lo que se toma un valor por defecto de "
                        + tasaCambioDefecto
        );

        ultimaFuenteTasa = "DEFECTO";

        return tasaCambioDefecto;
    }

    private Booster buscarBoosterVendido(String idBooster) {

        for (Usuario usuario : usuarios) {

            if (usuario instanceof Cliente cliente) {

                Producto producto = cliente
                        .getInventarioPersonal()
                        .buscarProducto(idBooster);

                if (producto instanceof Booster booster
                        && booster.estaVendido()) {

                    return booster;
                }
            }
        }

        return null;
    }

    public Compra comprarProducto(String idCliente, String idProducto) {

        Cliente cliente = buscarCliente(idCliente);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        Producto producto = inventario.buscarProducto(idProducto);

        if (producto == null) {

            if (buscarBoosterVendido(idProducto) != null) {
                throw new IllegalStateException("Booster ya vendido");
            }

            throw new IllegalArgumentException("Producto no encontrado");
        }

        if (producto instanceof Booster booster && booster.estaVendido()) {
            throw new IllegalStateException("Booster ya vendido");
        }

        double tasa = consultarTasaCambio();

        String fuente = ultimaFuenteTasa;

        Compra compra = new Compra(
                idCliente,
                List.of(producto),
                tasa,
                fuente
        );

        if (producto instanceof Booster booster) {
            booster.vender(idCliente);
        }

        inventario.retirarProducto(idProducto);
        cliente.recibirProducto(producto);
        cliente.agregarCompra(compra);
        compras.add(compra);

        return compra;
    }

    public List<Carta> abrirBooster(String idCliente, String idBooster) {

        Cliente cliente = buscarCliente(idCliente);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        Producto producto = cliente.getInventarioPersonal()
                .buscarProducto(idBooster);

        if (!(producto instanceof Booster booster)) {

            if (buscarBoosterVendido(idBooster) != null) {
                throw new SecurityException(
                        "El booster pertenece a otro cliente"
                );
            }

            Producto productoTienda = inventario.buscarProducto(idBooster);

            if (productoTienda instanceof Booster) {
                throw new IllegalStateException(
                        "El booster debe comprarse antes de abrirlo"
                );
            }

            throw new IllegalArgumentException("Booster no encontrado");
        }

        List<Carta> cartas = booster.abrir(idCliente);

        for (Carta carta : cartas) {
            cliente.recibirProducto(carta);
        }

        return cartas;
    }

    public void agregarTorneo(Torneo torneo) {
        if (torneo == null) {
            throw new IllegalArgumentException("Torneo inválido");
        }

        for (Torneo existente : torneos) {
            if (existente.getId().equals(torneo.getId())) {
                throw new IllegalStateException("El torneo ya existe");
            }
        }

        torneos.add(torneo);
    }

    public void inscribirEnTorneo(String idTorneo, String idCliente) {
        Cliente cliente = buscarCliente(idCliente);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        for (Torneo torneo : torneos) {
            if (torneo.getId().equals(idTorneo)) {
                torneo.inscribir(idCliente);
                cliente.inscribirEnTorneo(idTorneo);
                return;
            }
        }

        throw new IllegalArgumentException("Torneo no encontrado");
    }

    public List<Torneo> listarTorneos() {
        return new ArrayList<>(torneos);
    }

    public List<Compra> historialDeCliente(String idCliente) {
        Cliente cliente = buscarCliente(idCliente);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }

        return cliente.getHistorial();
    }

    public Inventario getInventario() {
        return inventario;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean esAdministradorRegistrado(Administrador admin) {
        return admin != null && admin.esAdmin() && usuarios.contains(admin);
    }


    public void restaurarReferencias() {

        for (Usuario usuario : usuarios) {

            if (usuario instanceof Cliente cliente) {

                for (Compra compra : cliente.getHistorial()) {

                    for (Producto producto : compra.getProductos()) {

                        Producto original = cliente
                                .getInventarioPersonal()
                                .buscarProducto(producto.getId());

                        if (original != null) {
                            compra.reemplazarProducto(original);
                        }
                    }
                }
            }
        }
    }

}
