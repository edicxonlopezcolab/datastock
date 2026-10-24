package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.Reader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;

public class ImportadorJson {

    public ResultadoImportacion importar(String textoRuta, Inventario inventario) 
    {

        ResultadoImportacion resultado = new ResultadoImportacion();

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

            for (JsonElement elemento : array) 
            {
                resultado.sumarEncontrado();
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
}
