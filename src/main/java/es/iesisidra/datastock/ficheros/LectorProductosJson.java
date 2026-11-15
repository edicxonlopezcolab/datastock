package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Reader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import es.iesisidra.datastock.validacion.ValidadorProducto;

/**
 * Componente que lee y valida un catálogo JSON de productos (RF-FIC-02).
 * <p>No modifica el inventario ni escribe en consola: devuelve un
 * {@link ResultadoLectura} con los productos válidos y las incidencias
 * detectadas para que el menú decida qué hacer con ellos.</p>
 */
public class LectorProductosJson {

    /**
     * Lee el fichero indicado, valida cada producto y acumula en el
     * resultado los válidos y las incidencias encontradas.
     * <p>No modifica el inventario ni imprime nada por consola.</p>
     * <p>No lanza excepciones: los fallos previstos (ruta nula, fichero
     * inexistente o ilegible, JSON mal formado, error de E/S, etc.)
     * se devuelven en el error general del resultado.</p>
     *
     * @param ruta ruta del fichero JSON a leer
     * @return resultado con los productos válidos leídos, las incidencias
     *         por cada producto rechazado, o un error general si el
     *         documento no puede interpretarse
     */
    public ResultadoLectura leer(Path ruta)
    {
        ResultadoLectura resultado = new ResultadoLectura();
        ValidadorProducto validador = new ValidadorProducto();

        if (ruta == null)
        {
            resultado.setErrorGeneral("la ruta es nula");
            return resultado;
        }

        if (!Files.exists(ruta))
        {
            resultado.setErrorGeneral("el fichero no existe");
            return resultado;
        }
        else if (!Files.isRegularFile(ruta))
        {
            resultado.setErrorGeneral("la ruta no corresponde a un fichero regular");
            return resultado;
        }
        else if (!Files.isReadable(ruta))
        {
            resultado.setErrorGeneral("el fichero no se puede leer");
            return resultado;
        }

        try (Reader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8))
        {
            JsonElement raiz = JsonParser.parseReader(lector);
            if (!raiz.isJsonObject())
            {
                resultado.setErrorGeneral("la raíz debe ser un objeto JSON");
                return resultado;
            }
            JsonObject objeto = raiz.getAsJsonObject();

            if (!objeto.has("productos") || !objeto.get("productos").isJsonArray())
            {
                resultado.setErrorGeneral("falta el array productos");
                return resultado;
            }
            JsonArray array = objeto.get("productos").getAsJsonArray();

            int posicion = 0;
            Set<String> codigosVistos = new HashSet<>();
            for (JsonElement elemento : array)
            {
                posicion++;
                resultado.sumarEncontrado();

                if (!elemento.isJsonObject())
                {
                    resultado.agregarIncidencia("Producto " + posicion + ": no es un objeto JSON");
                }
                else
                {
                    JsonObject producto = elemento.getAsJsonObject();
                    if (producto.size() == 0)
                    {
                        resultado.agregarIncidencia("Producto " + posicion + ": producto vacío, no tiene ningún campo");
                        continue;
                    }
                    String codigo = leerCampo(producto, "codigo");
                    String nombre = leerCampo(producto, "nombre");
                    String precio = leerCampo(producto, "precio");
                    String stock = leerCampo(producto, "stock");
                    String categoriaId = leerCampo(producto, "categoriaId");
                    String proveedorId = leerCampo(producto, "proveedorId");
                    List<String> errores = validador.validar(codigo, nombre, precio, stock, categoriaId, proveedorId);

                    String codigoMostrar;
                    if (codigo != null && !codigo.trim().isEmpty())
                    {
                        codigoMostrar = codigo;
                    }
                    else
                    {
                        codigoMostrar = "sin código";
                    }
                    if (!errores.isEmpty())
                    {
                        resultado.agregarIncidencia("Producto " + posicion + " (" + codigoMostrar + "): " + String.join("; ", errores));
                    }
                    else
                    {
                        String codigoNormalizado = ValidadorProducto.normalizarCodigo(codigo);
                        if (!codigosVistos.add(codigoNormalizado))
                        {
                            resultado.agregarIncidencia("Producto " + posicion + " (" + codigoMostrar + "): código repetido en el fichero");
                        }
                        else
                        {
                            resultado.agregarProducto(validador.crear(codigo, nombre, precio, stock, categoriaId, proveedorId));
                        }
                    }
                }
            }
        }
        catch (JsonParseException e)
        {
            resultado.setErrorGeneral("el fichero contiene un JSON mal formado");
        }
        catch (IOException e)
        {
            resultado.setErrorGeneral("se ha producido un error de lectura en el fichero");
        }

        return resultado;
    }

    /**
     * Lee un campo de un producto JSON como texto.
     * @return el valor como texto, o null si falta, es null o no es un valor simple
     */
    private String leerCampo(JsonObject producto, String nombre)
    {
        JsonElement valor = producto.get(nombre);
        if (valor == null || valor.isJsonNull())
        {
            return null;
        }
        if (valor.isJsonPrimitive())
        {
            return valor.getAsString();
        }
        return null;
    }

}