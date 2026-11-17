package Modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {

    private List<Compra> historialCompras;
    private Inventario inventarioPersonal;
    private List<String> torneosInscritos;

    public Cliente(String user, String pass) {
        super(user, pass, Rol.CLIENTE);
        this.historialCompras = new ArrayList<>();
        this.inventarioPersonal = new Inventario();
        this.torneosInscritos = new ArrayList<>();
    }

    public void agregarCompra(Compra c) {
        historialCompras.add(c);
    }

    public List<Compra> getHistorial() {
        return new ArrayList<>(historialCompras);
    }

    public void recibirProducto(Producto p) {
        inventarioPersonal.agregarProducto(p);
    }

    public Inventario getInventarioPersonal() {
        return inventarioPersonal;
    }

    public void inscribirEnTorneo(String idTorneo) {
        if (!estaInscritoEn(idTorneo)) {
            torneosInscritos.add(idTorneo);
        }
    }

    public boolean estaInscritoEn(String idTorneo) {
        return torneosInscritos.contains(idTorneo);
    }
}
