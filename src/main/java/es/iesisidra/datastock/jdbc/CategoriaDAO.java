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

    public void insertar(Categoria c) throws SQLException
    {
        String sql = "INSERT INTO categoria (id, nombre) VALUES(?, ?)";
        try (Connection con = ConexionBD.abrir();PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.executeUpdate();
        }
    }
}