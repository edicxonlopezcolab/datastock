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
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir())
        {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, nombre FROM categoria ORDER BY nombre");
            ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    lista.add(new Categoria(rs.getInt(1),rs.getString(2)));
                }
            }
            return lista;
        }
    }
}