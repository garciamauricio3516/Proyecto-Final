
package Persistencia;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class AdaptadorLocalDateTime
        implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    @Override
    public JsonElement serialize(LocalDateTime fecha, Type tipo,
                                 JsonSerializationContext contexto) {
        return new JsonPrimitive(fecha.toString());
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type tipo,
                                     JsonDeserializationContext contexto)
            throws JsonParseException {

        return LocalDateTime.parse(json.getAsString());
    }
}
