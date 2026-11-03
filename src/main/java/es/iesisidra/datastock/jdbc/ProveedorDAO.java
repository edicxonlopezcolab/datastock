package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla proveedor de MySQL (RF-JDBC-02).
 * <p>Mismo diseño que CategoriaDAO. El email es opcional: se guarda como NULL
 * cuando no se indica. No imprime nada: devuelve datos o lanza SQLException.</p>
 */
public class ProveedorDAO
{
    /**
     * Devuelve todos los proveedores ordenados por id.
     * @return lista de proveedores (vacía si no hay ninguno)
     * @throws SQLException si no se puede conectar o consultar
     */
    public List<Proveedor> listar() throws SQLException
    {
        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                lista.add(crearProveedor(rs));
            }
        }
        return lista;
    }

    /**
     * Busca un proveedor por su identificador.
     * @param id identificador del proveedor
     * @return el proveedor, o null si no existe
     * @throws SQLException si no se puede conectar o consultar
     */
    public Proveedor buscarPorId(int id) throws SQLException
    {
        String sql = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        try (Connection con = ConexionBD.abrir();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return crearProveedor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Da de alta un proveedor.
     * @param p proveedor con id, nombre y email (o null si no tiene)
     * @throws SQLException si el id o el email ya existen, o si falla la conexión
     */
    public void insertar(Proveedor p) throws SQLException
    {
        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ponerEmail(ps, 3, p.getEmail());
            ps.executeUpdate();
        }
    }

    /**
     * Cambia el nombre y el email de un proveedor. El id no se modifica.
     * @param p proveedor con el id existente y los datos nuevos
     * @return true si se actualizó, false si no existe ningún proveedor con ese id
     * @throws SQLException si el email ya existe o falla la conexión
     */
    public boolean actualizar(Proveedor p) throws SQLException
    {
        String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, p.getNombre());
            ponerEmail(ps, 2, p.getEmail());
            ps.setInt(3, p.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Elimina un proveedor por su identificador.
     * @param id identificador del proveedor
     * @return true si se eliminó, false si no existía
     * @throws SQLException si tiene productos asociados (clave foránea) o falla la conexión
     */
    public boolean eliminar(int id) throws SQLException
    {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection con = ConexionBD.abrir();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** Convierte la fila actual del ResultSet en un Proveedor. */
    private Proveedor crearProveedor(ResultSet rs) throws SQLException
    {
        return new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("email"));
    }

    /** Pone el email en la posición indicada, o NULL si no hay email. */
    private void ponerEmail(PreparedStatement ps, int posicion, String email) throws SQLException
    {
        if (email == null)
        {
            ps.setNull(posicion, Types.VARCHAR);
        }
        else
        {
            ps.setString(posicion, email);
        }
    }
}