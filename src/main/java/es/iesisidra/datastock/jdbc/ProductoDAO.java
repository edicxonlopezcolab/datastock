package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Acceso a la tabla producto de MySQL (RF-JDBC-03).
 * <p>La clave es el código. El precio se trata como BigDecimal (DECIMAL(10,2) en MySQL).
 * No imprime nada: devuelve datos o lanza SQLException.</p>
 */
public class ProductoDAO 
{
    /**
     * Busca un producto por su código.
     * @param codigo Código exacto del producto (ej. TEC-001).
     * @return El producto encontrado, o null si no existe.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public Producto buscarPorCodigo(String codigo) throws SQLException 
    {
        String sql = "SELECT codigo, nombre, precio, stock, categoria_id, proveedor_id FROM producto WHERE codigo = ?";

        try (Connection con = ConexionBD.abrir(); PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, codigo);
            
            try (ResultSet rs = ps.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return crearProducto(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserta un producto en la base de datos.
     * @param p Producto ya validado.
     * @throws SQLException Si ocurre un error SQL (clave duplicada, FK inexistente, etc.).
     */
    public void insertar(Producto p) throws SQLException 
    {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCategoriaId());
            ps.setInt(6, p.getProveedorId());
            
            ps.executeUpdate();
        }
    }

        /**
     * Modifica nombre, precio, categoría y proveedor de un producto.
     * El código y el stock no se modifican: el stock solo cambia con movimientos.
     * @param p producto con el código existente y los datos nuevos
     * @return true si se actualizó, false si no existe ningún producto con ese código
     * @throws SQLException si la categoría o el proveedor no existen, o falla la conexión
     */
    public boolean actualizar(Producto p) throws SQLException
    {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";
        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, p.getNombre());
            ps.setBigDecimal(2, p.getPrecio());
            ps.setInt(3, p.getCategoriaId());
            ps.setInt(4, p.getProveedorId());
            ps.setString(5, p.getCodigo());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Elimina un producto por su código.
     * @param codigo código del producto
     * @return true si se eliminó, false si no existía
     * @throws SQLException si tiene movimientos asociados (clave foránea) o falla la conexión
     */
    public boolean eliminar(String codigo) throws SQLException
    {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, codigo);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Crea un Producto a partir de la fila actual del ResultSet.
     * @param rs ResultSet posicionado en una fila válida.
     * @return Producto con los datos de esa fila.
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
