
package Persistencia;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;

public class AdaptadorLocalDate
        implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    @Override
    public JsonElement serialize(LocalDate fecha, Type tipo,
                                 JsonSerializationContext contexto) {
        return new JsonPrimitive(fecha.toString());
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type tipo,
                                 JsonDeserializationContext contexto)
            throws JsonParseException {

        return LocalDate.parse(json.getAsString());
    }
}
