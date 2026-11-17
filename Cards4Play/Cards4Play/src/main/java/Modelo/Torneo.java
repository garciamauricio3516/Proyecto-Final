
package Modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Torneo {

    private String id;
    private String nombre;
    private LocalDate fecha;
    private int maxParticipantes;
    private List<String> participantes;

    public Torneo(String nombre, LocalDate fecha, int maxParticipantes) {
        if (maxParticipantes <= 0) {
            throw new IllegalArgumentException("El cupo debe ser mayor a cero");
        }

        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.fecha = fecha;
        this.maxParticipantes = maxParticipantes;
        this.participantes = new ArrayList<>();
    }

    public void inscribir(String idCliente) {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Cliente inválido");
        }

        if (estaInscrito(idCliente)) {
            throw new IllegalStateException("El cliente ya está inscrito");
        }

        if (!hayCupo()) {
            throw new IllegalStateException("El torneo está lleno");
        }

        participantes.add(idCliente);
    }

    public boolean hayCupo() {
        return participantes.size() < maxParticipantes;
    }

    public int getCuposDisponibles() {
        return maxParticipantes - participantes.size();
    }

    public boolean estaInscrito(String idCliente) {
        return participantes.contains(idCliente);
    }

    public List<String> getParticipantes() {
        return new ArrayList<>(participantes);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }
}
