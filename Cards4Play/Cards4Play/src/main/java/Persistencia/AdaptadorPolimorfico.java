
package Persistencia;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Map;

public class AdaptadorPolimorfico<T>
        implements JsonSerializer<T>, JsonDeserializer<T> {

    private final String campoTipo;
    private final Map<String, Class<? extends T>> tipos;

    public AdaptadorPolimorfico(
            String campoTipo,
            Map<String, Class<? extends T>> tipos) {

        this.campoTipo = campoTipo;
        this.tipos = Map.copyOf(tipos);
    }

    @Override
    public JsonElement serialize(T objeto, Type tipo,
                                 JsonSerializationContext contexto) {

        JsonObject json = contexto.serialize(
                objeto, objeto.getClass()
        ).getAsJsonObject();

        json.addProperty(
                campoTipo,
                objeto.getClass().getSimpleName()
        );

        return json;
    }

    @Override
    public T deserialize(JsonElement json, Type tipo,
                         JsonDeserializationContext contexto)
            throws JsonParseException {

        JsonObject objeto = json.getAsJsonObject();

        if (!objeto.has(campoTipo)) {
            throw new JsonParseException("Falta el tipo del objeto");
        }

        String nombreTipo = objeto.get(campoTipo).getAsString();

        Class<? extends T> clase = tipos.get(nombreTipo);

        if (clase == null) {
            throw new JsonParseException(
                    "Tipo no permitido: " + nombreTipo
            );
        }

        return contexto.deserialize(json, clase);
    }
}
