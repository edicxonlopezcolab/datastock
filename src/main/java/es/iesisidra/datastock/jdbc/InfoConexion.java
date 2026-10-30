package es.iesisidra.datastock.jdbc;

/**
 * Datos de la base de datos obtenidos al comprobar la conexión (RF-JDBC-01).
 */
public class InfoConexion
{
    private final String baseDatos;
    private final String gestor;
    private final String version;
    private final int productos;

    public InfoConexion(String baseDatos, String gestor, String version, int productos)
    {
        this.baseDatos = baseDatos;
        this.gestor = gestor;
        this.version = version;
        this.productos = productos;
    }

    public String getBaseDatos() { return baseDatos; }
    public String getGestor() { return gestor; }
    public String getVersion() { return version; }
    public int getProductos() { return productos; }
}