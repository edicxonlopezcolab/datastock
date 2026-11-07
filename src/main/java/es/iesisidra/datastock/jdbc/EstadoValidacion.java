package es.iesisidra.datastock.jdbc;

/**
 * Estados posibles al validar un producto para la importación a MySQL.
 */
public enum EstadoValidacion 
{
    ACEPTADO,
    DUPLICADO,
    RECHAZADO
}