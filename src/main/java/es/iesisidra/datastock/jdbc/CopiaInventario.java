package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.validacion.ValidadorProducto;
import es.iesisidra.datastock.config.ConexionBD;
import java.sql.SQLException;
import java.util.List;
import java.sql.Connection;

/**
 * Copia el inventario en memoria a MySQL: valida cada producto e inserta 
 * los aceptados en una única transacción.
 */
public class CopiaInventario 
{
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ValidadorProducto validador = new ValidadorProducto();

    /**
     * Valida un producto en el orden requerido: duplicado, datos, categoría y proveedor.
     * @param p Producto a validar.
     * @return Resultado con el estado y, si aplica, el motivo del rechazo.
     * @throws SQLException Si falla la consulta a la base de datos.
     */
    public ResultadoValidacionProducto validar(Producto p) throws SQLException 
    {
        if (productoDAO.buscarPorCodigo(p.getCodigo()) != null) 
        {
            return new ResultadoValidacionProducto(EstadoValidacion.DUPLICADO, "El código ya existe en MySQL");
        }

        List<String> errores = validador.validar(
            p.getCodigo(),
            p.getNombre(),
            p.getPrecio() == null ? null : p.getPrecio().toPlainString(),
            String.valueOf(p.getStock()),
            String.valueOf(p.getCategoriaId()),
            String.valueOf(p.getProveedorId())
        );
        
        if (!errores.isEmpty()) 
        {
            return new ResultadoValidacionProducto(EstadoValidacion.RECHAZADO, String.join("; ", errores));
        }
        
        if (categoriaDAO.buscarPorId(p.getCategoriaId()) == null) 
        {
            return new ResultadoValidacionProducto(EstadoValidacion.RECHAZADO, "No existe la categoría con id " + p.getCategoriaId());
        }

        if (proveedorDAO.buscarPorId(p.getProveedorId()) == null) 
        {
            return new ResultadoValidacionProducto(EstadoValidacion.RECHAZADO, "No existe el proveedor con id " + p.getProveedorId());
        }
        
        return new ResultadoValidacionProducto(EstadoValidacion.ACEPTADO, null);
    }

    /**
     * Inserta un lote de productos aceptados dentro de una única transacción.
     * Si cualquier inserción falla, se deshace todo el lote (rollback).
     * @param aceptados Productos que ya pasaron la fase de validación.
     * @return Número de productos insertados.
     * @throws SQLException Si falla alguna inserción; tras hacer rollback.
     */
    public int insertarLote(List<Producto> aceptados) throws SQLException
    {
        if (aceptados == null || aceptados.isEmpty())
        {
            return 0;
        }
        int insertados = 0;
        try (Connection con = ConexionBD.abrir())
        {
            con.setAutoCommit(false);
            try
            {
                for (Producto p : aceptados)
                {
                    productoDAO.insertar(con, p);
                    insertados++;
                }
                con.commit();
            }
            catch (SQLException e)
            {
                con.rollback();
                throw e;
            }
            finally
            {
                con.setAutoCommit(true);
            }
        }
        return insertados;
    }
}