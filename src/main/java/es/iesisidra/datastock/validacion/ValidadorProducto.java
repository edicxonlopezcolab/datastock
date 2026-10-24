package es.iesisidra.datastock.validacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Valida y construye productos a partir de los seis campos en texto.
 * La usan tanto el importador JSON como el XML, para que ambos
 * apliquen exactamente las mismas reglas.
 */
public class ValidadorProducto
{
    /**
     * Comprueba todos los campos de un producto.
     * @return lista de errores encontrados; vacía si el producto es válido
     */
    public List<String> validar(String codigo, String nombre, String precio,
                                String stock, String categoriaId, String proveedorId)
    {
        List<String> errores = new ArrayList<>();
        validarCodigo(codigo, errores);
        validarNombre(nombre, errores);
        validarPrecio(precio, errores);
        validarEntero(stock, "stock", 0, errores);
        validarEntero(categoriaId, "categoriaId", 1, errores);
        validarEntero(proveedorId, "proveedorId", 1, errores);
        return errores;
    }

    private void validarCodigo(String codigo, List<String> errores)
    {
        if (codigo == null || codigo.trim().isEmpty())
        {
            errores.add("el código es obligatorio");
        }
        else
        {
            String codigoNormalizado = normalizarCodigo(codigo);
            if(codigoNormalizado.length() > 12 )
            {
                errores.add("el código no puede superar 12 caracteres");
            }

            String patron = "[A-Z0-9-]+";
            boolean esValido = codigoNormalizado.matches(patron);
        
            if (!esValido) 
            {
                errores.add("el código solo admite mayúsculas, números y guiones");
            } 
        }
    }

    private void validarNombre(String nombre, List<String> errores)
    {
        if (nombre == null || nombre.trim().isEmpty())
        {
            errores.add("el nombre es obligatorio");
        }
        else
        {
            String nombreNormalizado = nombre.trim();
            if(nombreNormalizado.length() < 3 || nombreNormalizado.length() > 100 )
            {
                errores.add("el nombre debe tener entre 3 y 100 caracteres");
            } 
        }
    }

    private void validarPrecio(String precio, List<String> errores)
    {
        if (precio == null || precio.trim().isEmpty())
        {
            errores.add("el precio es obligatorio");
        }
        else
        {
            try
            {
                BigDecimal precioNormalizado = new BigDecimal(precio.trim());
                if (precioNormalizado.compareTo(BigDecimal.ZERO) <= 0)
                {
                    errores.add("el precio debe ser mayor que 0");
                }
                else if (precioNormalizado.scale() > 2)
                {
                    errores.add("el precio admite como máximo dos decimales");
                }
            }
            catch (NumberFormatException e)
            {
                errores.add("el precio no es un número válido: " + precio);
            }
        }
    }

    private void validarEntero(String texto, String campo, int minimo, List<String> errores)
    {
        if (texto == null || texto.trim().isEmpty())
        {
            errores.add(campo + " es obligatorio");
        }
        else
        {
            try
            {
                int valor = Integer.parseInt(texto.trim());
                if (valor < minimo)
                {
                    errores.add(campo + " debe ser mayor o igual a " + minimo);
                }
            }
            catch (NumberFormatException e)
            {
                errores.add(campo + " no es un número entero válido: " + texto);
            }
        }
    }

    /**
     * Normaliza un código: sin espacios extremos y en mayúsculas.
     * Se usa al validar, al crear y al comparar duplicados.
     * @param codigo código original
     * @return código normalizado
     */
    public static String normalizarCodigo(String codigo)
    {
        return codigo.trim().toUpperCase();
    }


    /**
     * Construye el producto con los valores ya convertidos.
     * Solo debe llamarse si validar() no devolvió errores.
     */
    public Producto crear(String codigo, String nombre, String precio, 
                        String stock, String categoriaId, String proveedorId)
    {
        return new Producto(
            normalizarCodigo(codigo),
            nombre.trim(),
            new BigDecimal(precio.trim()),
            Integer.parseInt(stock.trim()),
            Integer.parseInt(categoriaId.trim()),
            Integer.parseInt(proveedorId.trim())
        );
    }
}