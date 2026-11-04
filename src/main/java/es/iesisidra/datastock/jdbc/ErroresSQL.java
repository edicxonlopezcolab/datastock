package es.iesisidra.datastock.jdbc;

import java.sql.SQLException;

/**
 * Traduce los errores de MySQL a mensajes comprensibles para el usuario.
 * <p>Nunca incluye la contraseña ni trazas técnicas.</p>
 */
public final class ErroresSQL
{
    private ErroresSQL() { }

    /**
     * @param e excepción lanzada por JDBC
     * @return mensaje en español que explica el problema
     */
    public static String explicar(SQLException e)
    {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        return switch (e.getErrorCode())
        {
            case 1045 -> "usuario o contraseña incorrectos";
            case 1049 -> "la base de datos datastock no existe; ejecuta el script SQL";
            case 1062 -> e.getMessage().contains("PRIMARY")
                        ? "ya existe un registro con ese identificador"
                        : "ya existe un registro con ese valor en un campo que no admite repetidos (nombre o email)";
            case 1451 -> "no se puede eliminar: tiene registros asociados (productos o movimientos) que dependen de él";
            case 1452 -> "la categoría o el proveedor indicado no existe (integridad referencial)";
            case 1644 -> "el procedimiento rechazó la operación: " + e.getMessage();
            default -> estado.startsWith("08")
                    ? "no se puede conectar con el servidor MySQL; comprueba que está arrancado"
                    : e.getMessage();
        };
    }
}