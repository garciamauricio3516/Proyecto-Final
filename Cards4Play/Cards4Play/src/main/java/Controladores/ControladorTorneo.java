
package Controladores;

import Modelo.*;
import Persistencia.PersistenciaJSON;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ControladorTorneo {

    private Tienda tienda;
    private PersistenciaJSON persistencia;

    public ControladorTorneo(Tienda tienda, PersistenciaJSON persistencia) {
        this.tienda = tienda;
        this.persistencia = persistencia;
    }

    public Torneo crearTorneo(Administrador admin, String nombre,
                              LocalDate fecha, int maxParticipantes) {
        validarAdmin(admin);

        Torneo torneo = admin.crearTorneo(nombre, fecha, maxParticipantes);
        tienda.agregarTorneo(torneo);

        guardarCambios();

        return torneo;
    }

    public void inscribirEnTorneo(String idTorneo, String idCliente) {

        tienda.inscribirEnTorneo(idTorneo, idCliente);

        guardarCambios();
    }

    public List<Torneo> listarTorneos() {
        return tienda.listarTorneos();
    }

    public Torneo buscarTorneo(String idTorneo) {
        for (Torneo torneo : tienda.listarTorneos()) {
            if (torneo.getId().equals(idTorneo)) {
                return torneo;
            }
        }
        return null;
    }

    public List<String> listarParticipantes(String idTorneo) {
        Torneo torneo = buscarTorneo(idTorneo);

        if (torneo == null) {
            throw new IllegalArgumentException("Torneo no encontrado");
        }

        return torneo.getParticipantes();
    }

    private void validarAdmin(Administrador admin) {
        if (!tienda.esAdministradorRegistrado(admin)) {
            throw new SecurityException("Administrador no autorizado");
        }
    }

    private void guardarCambios() {

        try {
            persistencia.guardar(tienda);

            System.out.println("Torneos guardados correctamente en JSON.");

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Se modificaron los torneos, pero no se pudo guardar en JSON.",
                    e
            );
        }
    }
}

