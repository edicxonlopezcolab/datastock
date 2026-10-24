package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

/**
 * Resumen de una importación: cuántos productos se encontraron,
 * cuántos se importaron, cuántos se rechazaron y por qué.
 * Si el documento entero es inválido, se indica con errorGeneral.
 */
public class ResultadoImportacion
{
    // ===== 1. LOS DATOS (atributos) =====
    private int encontrados;
    private int importados;
    private final List<String> incidencias = new ArrayList<>();
    private String errorGeneral;

    // ===== 2. MÉTODOS PARA ESCRIBIR (los usa el importador) =====

    /** Registra un producto leído del fichero. */
    public void sumarEncontrado()
    {
        encontrados++;
    }

    /** Registra un producto añadido al inventario. */
    public void sumarImportado()
    {
        importados++;
    }

    /** Registra el motivo de rechazo de un producto. */
    public void agregarIncidencia(String incidencia)
    {
        incidencias.add(incidencia);
    }

    /** Marca el documento completo como inválido (no se importa nada). */
    public void setErrorGeneral(String errorGeneral)
    {
        this.errorGeneral = errorGeneral;
    }

    // ===== 3. MÉTODOS PARA LEER (los usa el menú) =====

    public int getEncontrados()
    {
        return encontrados;
    }

    public int getImportados()
    {
        return importados;
    }

    public int getRechazados()
    {
        return encontrados - importados;
    }

    public List<String> getIncidencias()
    {
        return incidencias;
    }

    public String getErrorGeneral()
    {
        return errorGeneral;
    }

    /** @return true si el documento no pudo procesarse en absoluto. */
    public boolean hayErrorGeneral()
    {
        return errorGeneral != null;
    }
}