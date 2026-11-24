
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
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

public class PersistenciaJSON {

    private final Gson gson;
    private final Path rutaArchivo;

    public PersistenciaJSON(String ruta) {

        this.gson = ConfiguracionGson.crearGson();
        this.rutaArchivo = Path.of(ruta).toAbsolutePath();
    }

    public synchronized void guardar(Tienda tienda)
            throws IOException {

        if (tienda == null) {
            throw new IllegalArgumentException(
                    "La tienda no puede ser nula"
            );
        }

        Path carpeta = rutaArchivo.getParent();

        Files.createDirectories(carpeta);

        Path archivoTemporal = Files.createTempFile(
                carpeta,
                "tienda-",
                ".tmp"
        );

        try {

            // Escribir primero en un archivo temporal.

            try (Writer writer = Files.newBufferedWriter(
                    archivoTemporal,
                    StandardCharsets.UTF_8)) {

                gson.toJson(tienda, writer);
            }

            // Reemplazar el archivo original.

            try {

                Files.move(
                        archivoTemporal,
                        rutaArchivo,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        archivoTemporal,
                        rutaArchivo,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {

            Files.deleteIfExists(archivoTemporal);
        }
    }

    public synchronized Tienda cargar() throws IOException {

        if (!Files.exists(rutaArchivo)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(
                rutaArchivo,
                StandardCharsets.UTF_8)) {

            Tienda tienda = gson.fromJson(
                    reader,
                    Tienda.class
            );

            if (tienda == null) {
                throw new IOException(
                        "El archivo JSON está vacío"
                );
            }

            // Restaurar las referencias entre los objetos.

            tienda.restaurarReferencias();

            return tienda;

        } catch (JsonParseException e) {

            throw new IOException(
                    "Error al leer el archivo JSON",
                    e
            );
        }
    }
}
