package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.util.ColoresConsola;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Actividad comparativa obligatoria de SQLite (RA2-b).
 * <p>Clase ejecutable independiente que demuestra el uso de un gestor embebido.</p>
 * <p>Se ejecuta con:</p>
 * <pre>mvn exec:java "-Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite"</pre>
 */
public class DemoSQLite
{
    private static final String RUTA_DB = "datos/sqlite/productos_demo.db";
    private static final String URL = "jdbc:sqlite:" + RUTA_DB;

    /**
     * Punto de entrada de la demostración de SQLite.
     * @param args No se usan.
     */
    public static void main(String[] args)
    {
        System.out.println("\n===== DEMOSTRACIÓN SQLITE =====");
        // 1. Crear el directorio datos/sqlite si no existe
        try
        {
            Path directorio = Paths.get("datos/sqlite");
            if (!Files.exists(directorio))
            {
                Files.createDirectories(directorio);
            }
        }
        catch (IOException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] No se pudo crear el directorio: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }
        // 2. Abrir la conexión, crear la tabla e insertar
        try (Connection con = DriverManager.getConnection(URL))
        {
            System.out.println(ColoresConsola.pintar("[OK] Conexión SQLite abierta: " + RUTA_DB, ColoresConsola.VERDE));
            // 3. Crear la tabla (usando Statement)
            try (Statement st = con.createStatement())
            {
                String sqlCrear = "CREATE TABLE IF NOT EXISTS producto_demo (" +
                                  "codigo TEXT PRIMARY KEY NOT NULL, " +
                                  "nombre TEXT NOT NULL)";
                st.executeUpdate(sqlCrear);
                System.out.println(ColoresConsola.pintar("[OK] Tabla producto_demo disponible.", ColoresConsola.VERDE));
            }

            // 4. Insertar usando INSERT OR IGNORE (usando PreparedStatement)
            int insertados = 0;
            int existentes = 0;
            String sqlInsert = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlInsert))
            {
                // Producto A
                if (insertarProducto(ps, "SQL-001", "Producto demo A"))
                {
                    insertados++;
                }
                else
                {
                    existentes++;
                }
                // Producto B
                if (insertarProducto(ps, "SQL-002", "Producto demo B"))
                {
                    insertados++;
                }
                else
                {
                    existentes++;
                }
            }
            System.out.println("Insertados: " + insertados + " | Ya existentes: " + existentes);
            // 5. Consultar y mostrar los datos
            String sqlSelect = "SELECT codigo, nombre FROM producto_demo ORDER BY codigo";
            try (PreparedStatement ps = con.prepareStatement(sqlSelect);
                 ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    System.out.println(rs.getString("codigo") + " | " + rs.getString("nombre"));
                }
            }
        }
        catch (SQLException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] Error en la base de datos: " + e.getMessage(), ColoresConsola.ROJO));
        }
        // 6. Comprobación de persistencia del fichero
        Path fichero = Paths.get(RUTA_DB);
        if (Files.exists(fichero))
        {
            System.out.println(ColoresConsola.pintar("[OK] El fichero SQLite persiste en disco: " + fichero.toAbsolutePath(), ColoresConsola.VERDE));
        }
        else
        {
            System.out.println(ColoresConsola.pintar("[ERROR] El fichero SQLite no se ha creado.", ColoresConsola.ROJO));
        }
    }

    /**
     * Intenta insertar un producto con INSERT OR IGNORE.
     * @param ps PreparedStatement con la sentencia INSERT OR IGNORE.
     * @param codigo Código del producto.
     * @param nombre Nombre del producto.
     * @return true si se insertó, false si ya existía.
     * @throws SQLException si falla la ejecución.
     */
    private static boolean insertarProducto(PreparedStatement ps, String codigo, String nombre) throws SQLException
    {
        ps.setString(1, codigo);
        ps.setString(2, nombre);
        int filasAfectadas = ps.executeUpdate();
        
        if (filasAfectadas > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}