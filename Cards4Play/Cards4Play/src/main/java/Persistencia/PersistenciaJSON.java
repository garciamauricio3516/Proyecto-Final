
package Persistencia;

import Modelo.Tienda;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersistenciaJSON {

    private final Gson gson;
    private final Path rutaArchivo;

    public PersistenciaJSON(String ruta) {
        this.gson = ConfiguracionGson.crearGson();
        this.rutaArchivo = Path.of(ruta);
    }

    public void guardar(Tienda tienda) throws IOException {

        Path carpeta = rutaArchivo.toAbsolutePath().getParent();

        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }

        try (Writer writer = Files.newBufferedWriter(
                rutaArchivo, StandardCharsets.UTF_8)) {

            gson.toJson(tienda, writer);
        }
    }

    public Tienda cargar() throws IOException {

        if (!Files.exists(rutaArchivo)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(
                rutaArchivo, StandardCharsets.UTF_8)) {

            Tienda tienda = gson.fromJson(reader, Tienda.class);

            if (tienda == null) {
                throw new IOException("El archivo JSON está vacío");
            }

            // Restaurar referencias después de recuperar el JSON
            tienda.restaurarReferencias();

            return tienda;

        } catch (JsonParseException e) {
            throw new IOException("Error al leer el archivo JSON", e);
        }
    }
}

