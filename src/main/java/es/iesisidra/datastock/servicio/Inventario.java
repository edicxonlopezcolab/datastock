package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import es.iesisidra.datastock.validacion.ValidadorProducto;

/**
 * Estado común del inventario durante la ejecución de UT1.
 * <p>
 * Esta clase solo proporciona la colección compartida. No implementa
 * importación, validación, búsqueda, exportación ni persistencia: esos son
 * ejercicios que deberá desarrollar el alumnado.
 * </p>
 */
public class Inventario {
    private final List<Producto> productos = new ArrayList<>();

    /**
     * Obtiene la colección que comparten las opciones del menú.
     * <p>
     * ATENCIÓN: la lista es modificable. El alumnado deberá aplicar las
     * reglas del enunciado antes de añadir o sustituir elementos.
     * </p>
     */
    public List<Producto> getProductos() {
        return productos;
    }

    /**
     * Comprueba si ya existe un producto con ese código,
     * sin distinguir mayúsculas ni espacios extremos.
     * 
     * @param codigo código a buscar
     * @return true si ya está en el inventario
     */
    public boolean existeCodigo(String codigo) 
    {
        String codigoNormalizado = ValidadorProducto.normalizarCodigo(codigo);

        for (Producto p : productos) 
        {
            if (ValidadorProducto.normalizarCodigo(p.getCodigo()).equals(codigoNormalizado)) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Añade un producto solo si su código no existe todavía.
     * 
     * @param producto producto ya validado
     * @return true si se añadió; false si era duplicado
     */
    public boolean agregar(Producto producto) 
    {
        if (existeCodigo(producto.getCodigo())) 
        {
            return false;
        }
        productos.add(producto);
        return true;
    }

    /**
     * Busca un producto por su código, sin distinguir mayúsculas
     * ni espacios extremos.
     * @param codigo código a buscar
     * @return el producto encontrado, o null si no existe o el código está vacío
     */
    public Producto buscarPorCodigo(String codigo) 
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            return null;
        }

        String codigoNormalizado = ValidadorProducto.normalizarCodigo(codigo);

        for (Producto p : productos) 
        {
            if (ValidadorProducto.normalizarCodigo(p.getCodigo()).equals(codigoNormalizado)) 
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Busca productos cuyo nombre contenga el texto indicado,
     * sin distinguir mayúsculas ni espacios extremos.
     * 
     * @param texto texto a buscar en el nombre
     * @return lista con los productos encontrados (vacía si no hay coincidencias)
     */
    public List<Producto> buscarPorNombre(String texto) 
    {
        List<Producto> resultados = new ArrayList<>();

        if (texto == null || texto.trim().isEmpty()) 
        {
            return resultados;
        }

        String textoBuscado = texto.trim().toLowerCase();

        for (Producto p : productos) 
        {
            if (p.getNombre() != null && p.getNombre().toLowerCase().contains(textoBuscado)) 
            {
                resultados.add(p);
            }
        }
        return resultados;
    }

    /**
     * Devuelve una lista con todos los productos ordenada por código,
     * sin alterar el orden original del inventario.
     * 
     * @return lista ordenada de productos
     */
    public List<Producto> listarOrdenado() 
    {
        List<Producto> copia = new ArrayList<>(productos);
        copia.sort(Comparator.comparing(Producto::getCodigo));
        return copia;
    }
}
