package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Exporta productos a un catálogo JSON (RF-FIC-05).
 * <p>No imprime nada por consola; los errores de escritura se lanzan
 * para que el menú los muestre.</p>
 */
public class ExportadorJson 
{
    /**
     * Escribe los productos en un JSON con raíz "productos", en UTF-8.
     * @param productos productos a exportar, ya ordenados
     * @param destino fichero de destino
     * @return número de productos exportados
     * @throws IOException si no se puede escribir el fichero
     */
    public int exportar(List<Producto> productos, Path destino) throws IOException
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject raiz = new JsonObject();
        raiz.add("productos", gson.toJsonTree(productos));
        try(Writer escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8))
        {
            gson.toJson(raiz,escritor);
        }
        return productos.size();
    }
}
