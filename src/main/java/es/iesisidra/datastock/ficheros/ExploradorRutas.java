package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Explora rutas del sistema de archivos (RF-FIC-01).
 * Identifica si una ruta existe, si es fichero o directorio,
 * y muestra su información básica.
 */
public class ExploradorRutas
{
    /**
     * Explora la ruta indicada y muestra su información por consola.
     * @param texto ruta escrita por el usuario
     */
    public void explorar(String texto)
    {
        if (texto == null || texto.trim().isEmpty())
        {
            System.out.println("Error: la ruta está vacía.");
        }
        else
        {
            try
            {
                Path ruta = Paths.get(texto.trim());

                if (!Files.exists(ruta))
                {
                    System.out.println("La ruta no existe: " + ruta.toAbsolutePath());
                }
                else if (Files.isRegularFile(ruta))
                {
                    mostrarFichero(ruta);
                }
                else if (Files.isDirectory(ruta))
                {
                    listarDirectorio(ruta);
                }
                else
                {
                    System.out.println("La ruta existe pero no es un fichero ni un directorio.");
                }
            }
            catch (InvalidPathException e)
            {
                System.out.println("Error: La ruta escrita contiene caracteres no válidos.");
            }
            catch (Exception e)
            {
                System.out.println("Error inesperado al explorar la ruta: " + e.getMessage());
            }
        }
    }

    private void mostrarFichero(Path ruta)  
    {
        try 
        {
            String nombre = "";
            if (ruta.getFileName() != null) 
            {
                nombre = ruta.getFileName().toString();
            }

            Path rutaAbsoluta = ruta.toAbsolutePath();
            long tamanioFichero = Files.size(ruta);

            System.out.println("----- Información Fichero -----");
            System.out.println("Nombre: " + nombre);
            System.out.println("Ruta Absoluta: " + rutaAbsoluta);
            System.out.println("Tamaño: " + tamanioFichero + " bytes");
        } 
        catch (IOException e) 
        {
            System.out.println("Error al leer el fichero: " + e.getMessage());
        }
    }

    private void listarDirectorio(Path ruta) 
    {
        int ficheros = 0;
        int directorios = 0;

        try (Stream<Path> hijos = Files.list(ruta)) 
        {
            List<Path> lista = hijos.toList();
            
            if (lista.isEmpty()) 
            {
                System.out.println("Directorio vacío");
                return;
            }
            
            for (Path hijo : lista) 
            {
                if (Files.isDirectory(hijo)) 
                {
                    System.out.println("[DIR] " + hijo.getFileName());
                    directorios++;
                } 
                else 
                {
                    String infoTamanio;
                    try 
                    {
                        long tamanio = Files.size(hijo);
                        infoTamanio = tamanio + " bytes";
                    } 
                    catch (IOException e) 
                    {
                        infoTamanio = "tamaño no disponible";
                    }
                    
                    System.out.println("[FICHERO] " + hijo.getFileName() + " (" + infoTamanio + ")");
                    ficheros++;
                }
            }
            
            System.out.println("----------------------------------------");
            System.out.println("Total ficheros: " + ficheros + " | Directorios: " + directorios);

        } 
        catch (AccessDeniedException e) 
        {
            System.out.println("Error: No tienes permisos para acceder a este directorio.");
        } 
        catch (IOException e) 
        {
            System.out.println("Error de E/S al listar el directorio: " + e.getMessage());
        }
    }
}
