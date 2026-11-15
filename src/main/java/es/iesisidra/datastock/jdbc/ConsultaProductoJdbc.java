package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Componente de consulta de un producto por código.
 * <p>Usa JDBC con PreparedStatement, no escribe por consola y abre y cierra
 * su propia conexión usando {@code ConexionBD.abrir()}.</p>
 */
public class ConsultaProductoJdbc
{
    /**
     * Busca un producto por su código exacto en MySQL.
     * <p>El código debe venir normalizado (en mayúsculas y sin espacios extremos).</p>
     * <p>Si el código es nulo o está vacío, devuelve un Optional vacío sin
     * consultar la base de datos.</p>
     * 
     * @param codigo Código del producto.
     * @return Un Optional con el producto si existe, o vacío si no se encuentra.
     * @throws SQLException Si ocurre un error de conexión, credenciales incorrectas,
     *                      esquema inexistente o error en la consulta SQL.
     */
    public Optional<Producto> buscarPorCodigo(String codigo) throws SQLException
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            return Optional.empty();
        }

        String sql = "SELECT codigo, nombre, precio, stock, categoria_id, proveedor_id "
                   + "FROM producto WHERE codigo = ?";

        try (Connection con = ConexionBD.abrir();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return Optional.of(crearProducto(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Producto.
     * @param rs ResultSet posicionado en una fila válida.
     * @return Producto con los datos de la fila.
     * @throws SQLException Si falla la lectura de alguna columna.
     */
    private Producto crearProducto(ResultSet rs) throws SQLException
    {
        return new Producto(
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getBigDecimal("precio"),
            rs.getInt("stock"),
            rs.getInt("categoria_id"),
            rs.getInt("proveedor_id")
        );
    }
}