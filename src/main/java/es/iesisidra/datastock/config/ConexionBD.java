package es.iesisidra.datastock.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones con la base de datos MySQL de DataStock.
 * <p>Las credenciales se leen de db.properties (src/main/resources),
 * que está excluido de Git para no publicar contraseñas.</p>
 */
public final class ConexionBD
{
    private ConexionBD() { }

    /**
     * Abre una conexión nueva con MySQL. Quien la pide debe cerrarla,
     * preferiblemente con try-with-resources.
     * @return conexión abierta
     * @throws SQLException si falta la configuración o no se puede conectar
     */
    public static Connection abrir() throws SQLException
    {
        Properties datos = new Properties();
        try (InputStream entrada = ConexionBD.class.getClassLoader()
                .getResourceAsStream("db.properties"))
        {
            if (entrada == null)
            {
                throw new SQLException("No se encuentra db.properties (copia db.properties.example)");
            }
            datos.load(entrada);
        }
        catch (IOException e)
        {
            throw new SQLException("Error leyendo db.properties", e);
        }

        String url = datos.getProperty("jdbc.url");
        String usuario = datos.getProperty("jdbc.user");
        String clave = datos.getProperty("jdbc.password");
        if (url == null || usuario == null || clave == null)
        {
            throw new SQLException("Faltan jdbc.url, jdbc.user o jdbc.password en db.properties");
        }
        return DriverManager.getConnection(url, usuario, clave);
    }
}