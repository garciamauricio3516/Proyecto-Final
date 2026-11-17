
package Persistencia;

import Modelo.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class ConfiguracionGson {

    public static Gson crearGson() {

        GsonBuilder builder = new GsonBuilder();

        builder.setPrettyPrinting();

        // Adaptador para los usuarios
        builder.registerTypeAdapter(
                Usuario.class,
                new AdaptadorPolimorfico<Usuario>(
                        "_tipo",
                        Map.of(
                                "Administrador", Administrador.class,
                                "Cliente", Cliente.class
                        )
                )
        );

        // Adaptador para los productos
        builder.registerTypeAdapter(
                Producto.class,
                new AdaptadorPolimorfico<Producto>(
                        "_tipo",
                        Map.of(
                                "Carta", Carta.class,
                                "Booster", Booster.class,
                                "ProductoSellado", ProductoSellado.class,
                                "Accesorio", Accesorio.class
                        )
                )
        );

        // Adaptador para LocalDate
        builder.registerTypeAdapter(
                LocalDate.class,
                new AdaptadorLocalDate()
        );

        // Adaptador para LocalDateTime
        builder.registerTypeAdapter(
                LocalDateTime.class,
                new AdaptadorLocalDateTime()
        );

        return builder.create();
    }
}
