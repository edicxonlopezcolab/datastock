package es.iesisidra.datastock.jdbc;

/**
 * Resultado de validar un producto antes de insertarlo en MySQL.
 * Contiene el estado y el motivo (solo si hay rechazo o duplicado).
 */
public class ResultadoValidacionProducto 
{
    private final EstadoValidacion estado;
    private final String motivo;

    public ResultadoValidacionProducto(EstadoValidacion estado, String motivo) 
    {
        this.estado = estado;
        this.motivo = motivo;
    }

    public EstadoValidacion getEstado() { return estado; }
    public String getMotivo() { return motivo; }
}