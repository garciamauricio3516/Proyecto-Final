
package Modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Compra {

    private String id;
    private String idCliente;
    private List<Producto> productos;
    private LocalDateTime fecha;
    private double totalUsd;
    private double totalCop;
    private String fuenteTasa;

    public Compra(String idCliente, List<Producto> productos,
                  double tasaCambio, String fuenteTasa) {

        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Cliente inválido");
        }

        if (productos == null || productos.isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener productos");
        }

        if (!Double.isFinite(tasaCambio) || tasaCambio <= 0) {
            throw new IllegalArgumentException("Tasa de cambio inválida");
        }

        if (!"API".equals(fuenteTasa) && !"DEFECTO".equals(fuenteTasa)) {
            throw new IllegalArgumentException("Fuente de tasa inválida");
        }

        this.id = UUID.randomUUID().toString();
        this.idCliente = idCliente;
        this.productos = new ArrayList<>(productos);
        this.fecha = LocalDateTime.now();
        this.totalUsd = calcularTotalUsd();
        this.totalCop = totalUsd * tasaCambio;
        this.fuenteTasa = fuenteTasa;
    }

    public double calcularTotalUsd() {
        double total = 0;

        for (Producto producto : productos) {
            total += producto.getPrecioUsd();
        }

        return total;
    }

    public List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public double getTotalCop() {
        return totalCop;
    }

    public String getFuenteTasa() {
        return fuenteTasa;
    }

    public String getId() {
        return id;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public double getTotalUsd() {
        return totalUsd;
    }


    public void reemplazarProducto(Producto productoOriginal) {

        for (int i = 0; i < productos.size(); i++) {

            Producto producto = productos.get(i);

            if (producto.getId().equals(productoOriginal.getId())) {
                productos.set(i, productoOriginal);
            }
        }
    }

}
