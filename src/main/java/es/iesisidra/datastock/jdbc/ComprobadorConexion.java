package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.config.ConexionBD;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Comprueba la conexión con MySQL y obtiene información de la base de datos (RF-JDBC-01).
 * <p>No imprime nada: devuelve los datos para que el menú los muestre.</p>
 */

public class ComprobadorConexion
{
    /**
     * Abre una conexión, lee los metadatos y cuenta los productos.
     * La conexión y la sentencia se cierran al terminar, aunque haya un error.
     * @return nombre de la base, gestor, versión y número de productos
     * @throws SQLException si no se puede conectar o consultar
     */
    public InfoConexion comprobar() throws SQLException
    {
        try (Connection con = ConexionBD.abrir())
        {
            DatabaseMetaData meta = con.getMetaData();

            String baseDatos = con.getCatalog();
            String gestor    = meta.getDatabaseProductName();
            String version   = meta.getDatabaseProductVersion();

            int productos;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM producto");
            ResultSet rs = ps.executeQuery())
            {
                rs.next();
                productos = rs.getInt(1);
            }

            return new InfoConexion(baseDatos, gestor, version, productos);
        }
    }
}