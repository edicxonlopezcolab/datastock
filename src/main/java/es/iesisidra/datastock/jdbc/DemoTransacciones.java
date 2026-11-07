package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demostración de control de transacciones JDBC (RF-JDBC-08).
 * <p>No imprime nada por consola: devuelve resultados para que el menú los pinte.</p>
 * <p>Usa los IDs 901, 902 y 903, reservados exclusivamente para la demo.
 * Los nombres empiezan por DEMO-TX para que no choquen con categorías reales.</p>
 */
public class DemoTransacciones
{
    public static final int ID_A1 = 901;
    public static final int ID_A2 = 902;
    public static final int ID_B1 = 903;

    public static final String NOMBRE_A1 = "DEMO-TX-901";
    public static final String NOMBRE_A2 = "DEMO-TX-902";
    public static final String NOMBRE_B1 = "DEMO-TX-903";

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    /**
     * Devuelve los IDs de prueba que ya existen en MySQL.
     * Si la lista está vacía, se puede ejecutar la demo.
     * @return lista de IDs ocupados (901, 902, 903).
     * @throws SQLException si falla la consulta.
     */
    public List<Integer> idsOcupados() throws SQLException
    {
        List<Integer> ocupados = new ArrayList<>();
        for (int id : new int[]{ID_A1, ID_A2, ID_B1})
        {
            if (categoriaDAO.buscarPorId(id) != null)
            {
                ocupados.add(id);
            }
        }
        return ocupados;
    }

    /**
     * Escenario A: inserta 901 y 902 dentro de una transacción y hace commit.
     * Si algo falla, hace rollback y relanza.
     * @throws SQLException si falla la inserción o el commit.
     */
    public void ejecutarEscenarioA() throws SQLException
    {
        try (Connection con = ConexionBD.abrir())
        {
            con.setAutoCommit(false);
            try
            {
                categoriaDAO.insertar(con, new Categoria(ID_A1, NOMBRE_A1));
                categoriaDAO.insertar(con, new Categoria(ID_A2, NOMBRE_A2));
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
    }

    /**
     * Escenario B: inserta 903, luego intenta insertar 903 otra vez (duplicado).
     * El duplicado debe lanzar SQLException con código 1062. Hace rollback para
     * deshacer la primera inserción y relanza la excepción esperada.
     * @throws SQLException si el duplicado no se detecta o si falla otra cosa.
     */
    public void ejecutarEscenarioB() throws SQLException
    {
        try (Connection con = ConexionBD.abrir())
        {
            con.setAutoCommit(false);
            try
            {
                categoriaDAO.insertar(con, new Categoria(ID_B1, NOMBRE_B1));
                categoriaDAO.insertar(con, new Categoria(ID_B1, NOMBRE_B1));

                throw new SQLException(
                    "El escenario B no lanzó el error esperado: el duplicado no se detectó");
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
    }
}