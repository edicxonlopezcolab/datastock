package es.iesisidra.datastock.ficheros;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Representa el resultado de leer un fichero de productos en formato JSON.
 * <p>Es una clase de transferencia de datos (DTO) que transporta los productos
 * válidos encontrados y las incidencias producidas durante la lectura.</p>
 */
public class ResultadoLectura
{
    private final List<Producto> productosValidos = new ArrayList<>();
    private int encontrados;
    private final List<String> incidencias = new ArrayList<>();
    private String errorGeneral;

    /**
     * Registra que se ha leído un producto del fichero, sea válido o no.
     */
    public void sumarEncontrado()
    {
        encontrados++;
    }

    /**
     * Añade un producto a la lista de válidos.
     * @param p Producto que ha superado la validación.
     */
    public void agregarProducto(Producto p)
    {
        productosValidos.add(p);
    }

    /**
     * Registra el motivo por el que un producto ha sido rechazado.
     * @param incidencia Texto descriptivo del error.
     */
    public void agregarIncidencia(String incidencia)
    {
        incidencias.add(incidencia);
    }

    /**
     * Marca el documento completo como inválido (no se puede leer).
     * @param errorGeneral Texto descriptivo del problema estructural.
     */
    public void setErrorGeneral(String errorGeneral)
    {
        this.errorGeneral = errorGeneral;
    }

    /** @return lista de productos que han superado la validación. */
    public List<Producto> getProductos() { return productosValidos; }

    /** @return número total de productos leídos del fichero. */
    public int getEncontrados() { return encontrados; }

    /** @return lista con los motivos de rechazo o incidencias encontradas. */
    public List<String> getIncidencias() { return incidencias; }

    /** @return mensaje de error general, o null si el documento es procesable. */
    public String getErrorGeneral() { return errorGeneral; }

    /** @return true si el documento no ha podido procesarse en absoluto. */
    public boolean hayErrorGeneral() { return errorGeneral != null; }
}