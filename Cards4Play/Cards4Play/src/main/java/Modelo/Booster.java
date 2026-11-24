
package Modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Booster extends Producto {

    private List<Carta> cartas;
    private boolean vendido;
    private boolean abierto;
    private String idPropietario;

    private static final Random RANDOM = new Random();

    public Booster(double precioUsd) {
        super("Booster", precioUsd);
        this.cartas = new ArrayList<>();
        this.vendido = false;
        this.abierto = false;
        this.idPropietario = null;
    }

    public void generarContenido(List<Carta> cartasDisponibles) {

        if (cartasDisponibles == null || cartasDisponibles.isEmpty()) {
            throw new IllegalArgumentException("No hay cartas registradas");
        }

        if (!cartas.isEmpty()) {
            throw new IllegalStateException("El booster ya tiene contenido");
        }

        List<Carta> contenido = new ArrayList<>();
        Set<String> cartasUtilizadas = new HashSet<>();

        for (int i = 0; i < 10; i++) {

            Rareza rareza = Rareza.sortear();

            Carta carta = elegirCarta(
                    rareza,
                    cartasDisponibles,
                    cartasUtilizadas
            );

            if (carta == null) {
                carta = aplicarReglaRespaldo(
                        rareza,
                        cartasDisponibles,
                        cartasUtilizadas
                );
            }

            contenido.add(copiarCarta(carta));
            cartasUtilizadas.add(carta.getId());
        }

        garantizarCartaRara(
                contenido,
                cartasDisponibles,
                cartasUtilizadas
        );

        this.cartas = contenido;
    }

    private Carta copiarCarta(Carta carta) {

        return new Carta(
                carta.getNombre(),
                carta.getRareza(),
                carta.getPrecioUsd()
        );
    }

    private Carta elegirCarta(
            Rareza rareza,
            List<Carta> disponibles,
            Set<String> utilizadas) {

        List<Carta> opciones = new ArrayList<>();
        List<Carta> opcionesSinRepetir = new ArrayList<>();

        for (Carta carta : disponibles) {

            if (carta.getRareza() == rareza) {

                opciones.add(carta);

                if (!utilizadas.contains(carta.getId())) {
                    opcionesSinRepetir.add(carta);
                }
            }
        }

        if (!opcionesSinRepetir.isEmpty()) {

            return opcionesSinRepetir.get(
                    RANDOM.nextInt(opcionesSinRepetir.size())
            );
        }

        if (opciones.isEmpty()) {
            return null;
        }

        return opciones.get(
                RANDOM.nextInt(opciones.size())
        );
    }

    private Carta aplicarReglaRespaldo(
            Rareza rareza,
            List<Carta> disponibles,
            Set<String> utilizadas) {

        Rareza inferior = rareza.inferior();

        while (inferior != null) {

            Carta carta = elegirCarta(
                    inferior,
                    disponibles,
                    utilizadas
            );

            if (carta != null) {
                return carta;
            }

            inferior = inferior.inferior();
        }

        Rareza superior = rareza.superior();

        while (superior != null) {

            Carta carta = elegirCarta(
                    superior,
                    disponibles,
                    utilizadas
            );

            if (carta != null) {
                return carta;
            }

            superior = superior.superior();
        }

        throw new IllegalStateException(
                "No hay cartas disponibles"
        );
    }

    private void garantizarCartaRara(
            List<Carta> contenido,
            List<Carta> disponibles,
            Set<String> utilizadas) {

        for (Carta carta : contenido) {

            if (carta.getRareza().ordinal()
                    >= Rareza.R.ordinal()) {

                return;
            }
        }

        // Bonificacion: garantizar una carta R o superior
        // cuando exista alguna registrada en el inventario.

        List<Carta> candidatas = new ArrayList<>();

        for (Carta carta : disponibles) {

            if (carta.getRareza().ordinal()
                    >= Rareza.R.ordinal()) {

                candidatas.add(carta);
            }
        }

        if (candidatas.isEmpty()) {
            return;
        }

        Carta cartaRara = candidatas.get(
                RANDOM.nextInt(candidatas.size())
        );

        // Solo se reemplaza la ultima carta.
        // Las otras nueve conservan su seleccion original.

        contenido.set(
                contenido.size() - 1,
                copiarCarta(cartaRara)
        );

        utilizadas.add(cartaRara.getId());
    }

    public void vender(String idCliente) {

        if (vendido) {
            throw new IllegalStateException(
                    "El booster ya fue vendido"
            );
        }

        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "Cliente inválido"
            );
        }

        if (cartas.size() != 10) {
            throw new IllegalStateException(
                    "El booster no tiene 10 cartas"
            );
        }

        this.vendido = true;
        this.idPropietario = idCliente;
    }

    public List<Carta> abrir(String idCliente) {

        if (!vendido || !esPropietario(idCliente)) {
            throw new SecurityException(
                    "No eres el propietario del booster"
            );
        }

        if (abierto) {
            throw new IllegalStateException(
                    "El booster ya fue abierto"
            );
        }

        abierto = true;

        return new ArrayList<>(cartas);
    }

    public boolean estaVendido() {
        return vendido;
    }

    public boolean estaAbierto() {
        return abierto;
    }

    public boolean esPropietario(String idCliente) {

        return idPropietario != null
                && idPropietario.equals(idCliente);
    }

    public List<Carta> getCartasSiAbierto() {

        if (!abierto) {
            return new ArrayList<>();
        }

        return new ArrayList<>(cartas);
    }

    @Override
    public String getTipo() {
        return "BOOSTER";
    }
}
