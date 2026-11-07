package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;

/**
 * Devuelve todas las categorías de MySQL ordenadas por nombre.
 * @return lista de categorías (vacía si no hay ninguna)
 * @throws SQLException si no se puede conectar o consultar
 */
public class CategoriaDAO
{
    public List<Categoria> listar() throws SQLException
{
    String sql = "SELECT id, nombre FROM categoria ORDER BY id";
    List<Categoria> lista = new ArrayList<>();

    try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery())
    {
        while (rs.next())
        {
            lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
        }
    }
    return lista;
}
    /**
     * Busca una categoría por su identificador.
     * @param id identificador de la categoría
     * @return la categoría, o null si no existe
     * @throws SQLException si no se puede conectar o consultar
     */
    public Categoria buscarPorId(int id) throws SQLException 
    {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";

        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Inserta una categoría usando una conexión ya abierta.
     * NO cierra la conexión (la cierra quien la abrió).
     * @param con Conexión abierta por el llamador.
     * @param c Categoría a insertar.
     * @throws SQLException Si ocurre un error SQL.
     */
    public void insertar(Connection con, Categoria c) throws SQLException
    {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.executeUpdate();
        }
    }

    /**
     * Inserta una categoría abriendo su propia conexión.
     * Delega en la sobrecarga que recibe la conexión.
     */
    public void insertar(Categoria c) throws SQLException
    {
        try (Connection con = ConexionBD.abrir())
        {
            insertar(con, c);
        }
    }

    /**
     * Cambia el nombre de una categoría. El id no se modifica.
     * @param c categoría con el id existente y el nombre nuevo
     * @return true si se actualizó, false si no existe ninguna categoría con ese id
     * @throws SQLException si el nombre ya existe o falla la conexión
     */
    public boolean actualizar(Categoria c) throws SQLException
    {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            int filas = ps.executeUpdate();
            return filas == 1;
        }
    }

    /**
     * Elimina una categoría por su identificador.
     * @param id identificador de la categoría
     * @return true si se eliminó, false si no existía
     * @throws SQLException si tiene productos asociados (clave foránea) o falla la conexión
     */
    public boolean eliminar(int id) throws SQLException
    {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            return filas == 1;
        }
    }
}