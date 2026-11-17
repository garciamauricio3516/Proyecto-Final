package Modelo;

import java.time.LocalDate;
import java.util.List;

public class Administrador extends Usuario {

    public Administrador(String user, String pass) {
        super(user, pass, Rol.ADMIN);
    }

    public Booster crearBooster(double precioUsd, List<Carta> cartas) {
        Booster booster = new Booster(precioUsd);
        booster.generarContenido(cartas);
        return booster;
    }

    public Carta registrarCarta(String nombre, Rareza rareza, double precioUsd) {
        return new Carta(nombre, rareza, precioUsd);
    }

    public ProductoSellado registrarProductoSellado(String nombre, double precioUsd) {
        return new ProductoSellado(nombre, precioUsd);
    }

    public Accesorio registrarAccesorio(String nombre, double precioUsd) {
        return new Accesorio(nombre, precioUsd);
    }

    public Torneo crearTorneo(String nombre, LocalDate fecha, int maxParticipantes) {
        return new Torneo(nombre, fecha, maxParticipantes);
    }
}