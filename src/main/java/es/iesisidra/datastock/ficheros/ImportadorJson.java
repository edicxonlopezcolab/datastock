package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.Reader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class ImportadorJson {

    public ResultadoImportacion importar(String textoRuta, Inventario inventario) 
    {

        ResultadoImportacion resultado = new ResultadoImportacion();
        ValidadorProducto validador = new ValidadorProducto();

        if (textoRuta == null || textoRuta.trim().isEmpty()) 
        {
            resultado.setErrorGeneral("la ruta está vacía");
            return resultado;
        }

        Path ruta;
        try 
        {
            ruta = Paths.get(textoRuta.trim());
        } 
        catch (InvalidPathException e) 
        {
            resultado.setErrorGeneral("la ruta no es válida");
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
                    String codigo = leerCampo(producto, "codigo");
                    String nombre = leerCampo(producto, "nombre");
                    String precio = leerCampo(producto, "precio");
                    String stock = leerCampo(producto, "stock");
                    String categoriaId = leerCampo(producto, "categoriaId");
                    String proveedorId = leerCampo(producto, "proveedorId");
                    List<String> errores = validador.validar(codigo, nombre, precio, stock, categoriaId, proveedorId);

                    if (!errores.isEmpty()) 
                    {
                        resultado.agregarIncidencia("Producto " + posicion + " (" + codigo + "): " + String.join("; ", errores));
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
