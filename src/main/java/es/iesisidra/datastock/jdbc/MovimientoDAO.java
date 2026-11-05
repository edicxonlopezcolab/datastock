package es.iesisidra.datastock.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.MovimientoStock;

public class MovimientoDAO
{
    public void registrar(String codigo, String tipo, int cantidad) throws SQLException
    {
        String sql = "{call registrar_movimiento(?, ?, ?)}";
        try (Connection con = ConexionBD.abrir(); CallableStatement cs = con.prepareCall(sql))
        {
            cs.setString(1, codigo);
            cs.setString(2, tipo);
            cs.setInt(3, cantidad);
            cs.execute();
        }
    }

    public MovimientoStock ultimoDeProducto(String codigo) throws SQLException
    {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, "
                   + "stock_anterior, stock_posterior "
                   + "FROM movimiento_stock "
                   + "WHERE producto_codigo = ? "
                   + "ORDER BY fecha DESC, id DESC "
                   + "LIMIT 1";

        try (Connection con = ConexionBD.abrir(); PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<MovimientoStock> listarTodos() throws SQLException
    {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, "
                   + "stock_anterior, stock_posterior "
                   + "FROM movimiento_stock "
                   + "ORDER BY fecha ASC, id ASC";
        List<MovimientoStock> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<MovimientoStock> listarPorProducto(String codigo) throws SQLException
    {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, "
                   + "stock_anterior, stock_posterior "
                   + "FROM movimiento_stock "
                   + "WHERE producto_codigo = ? "
                   + "ORDER BY fecha ASC, id ASC";
        List<MovimientoStock> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(); PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private MovimientoStock mapear(ResultSet rs) throws SQLException
    {
        return new MovimientoStock(
            rs.getLong("id"),
            rs.getString("producto_codigo"),
            MovimientoStock.TipoMovimiento.valueOf(rs.getString("tipo")),
            rs.getInt("cantidad"),
            rs.getObject("fecha", LocalDateTime.class),
            rs.getInt("stock_anterior"),
            rs.getInt("stock_posterior")
        );
    }
}
