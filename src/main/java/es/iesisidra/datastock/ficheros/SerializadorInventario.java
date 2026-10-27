package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.validacion.ValidadorProducto;

/**
 * Guarda y recupera el inventario completo en un fichero binario .dat (RF-FIC-06).
 * <p>No imprime nada por consola; los errores se lanzan como IOException
 * para que el menú los muestre.</p>
 */
public class SerializadorInventario
{
    /**
     * Escribe la lista completa de productos en el fichero de destino.
     * @param productos productos a guardar, ya ordenados
     * @param destino fichero binario de destino
     * @return número de productos guardados
     * @throws IOException si no se puede escribir el fichero
     */
    public int guardar(List<Producto> productos, Path destino) throws IOException
    {
        try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(destino)))
        {
            salida.writeObject(new ArrayList<>(productos));
        }
        return productos.size();
    }

    /**
     * Lee un inventario guardado con {@link #guardar} y comprueba que contiene
     * una lista de productos válidos.
     * @param origen fichero .dat generado por DataStock
     * @return productos recuperados
     * @throws IOException si el fichero no se puede leer, está dañado o no
     *         contiene un inventario válido
     */
    public List<Producto> recuperar(Path origen) throws IOException
    {
        ValidadorProducto validador = new ValidadorProducto();
        List<Producto> recuperados = new ArrayList<>();

        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(origen)))
        {
            Object leido = entrada.readObject();

            if (!(leido instanceof List<?>))
            {
                throw new IOException("el fichero no contiene un inventario");
            }

            for (Object elemento : (List<?>) leido)
            {
                if (!(elemento instanceof Producto))
                {
                    throw new IOException("el fichero no contiene un inventario");
                }
                Producto p = (Producto) elemento;

                List<String> errores = validador.validar(
                    p.getCodigo(),
                    p.getNombre(),
                    p.getPrecio() == null ? null : p.getPrecio().toPlainString(),
                    String.valueOf(p.getStock()),
                    String.valueOf(p.getCategoriaId()),
                    String.valueOf(p.getProveedorId()));

                if (!errores.isEmpty())
                {
                    throw new IOException("producto no válido en el fichero: " + String.join("; ", errores));
                }
                recuperados.add(p);
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("formato de fichero no reconocido", e);
        }

        return recuperados;
    }
}