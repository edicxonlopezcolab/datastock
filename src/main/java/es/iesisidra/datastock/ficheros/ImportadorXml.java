package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.validacion.ValidadorProducto;

/**
 * Importa productos desde un catálogo XML (RF-FIC-03).
 * <p>No imprime nada por consola: devuelve un {@link ResultadoImportacion}
 * que el menú se encarga de mostrar.</p>
 */
public class ImportadorXml 
{
    /**
     * Lee el fichero, valida cada producto y añade los válidos al inventario.
     * Si el documento no puede interpretarse, no se modifica el inventario.
     * @param textoRuta ruta del fichero XML escrita por el usuario
     * @param inventario inventario común donde se añaden los productos
     * @return resumen con encontrados, importados, rechazados e incidencias,
     *         o un error general si el documento no es válido
     */
    public ResultadoImportacion importar(String textoRuta, Inventario inventario) 
    {

        ResultadoImportacion resultado = new ResultadoImportacion();
        ValidadorProducto validador = new ValidadorProducto();

        if (textoRuta == null || textoRuta.trim().isEmpty()) 
        {
            resultado.setErrorGeneral("la ruta está vacía");
            return resultado;
        }

        Path ruta;
        try 
        {
            ruta = Paths.get(textoRuta.trim());
        } 
        catch (InvalidPathException e) 
        {
            resultado.setErrorGeneral("la ruta no es válida");
            return resultado;
        }

        if (!Files.exists(ruta)) 
        {
            resultado.setErrorGeneral("el fichero no existe");
            return resultado;
        } 
        else if (!Files.isRegularFile(ruta)) 
        {
            resultado.setErrorGeneral("la ruta no corresponde a un fichero regular");
            return resultado;
        } 
        else if (!Files.isReadable(ruta)) 
        {
            resultado.setErrorGeneral("el fichero no se puede leer");
            return resultado;
        }

        try 
        {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);

            DocumentBuilder analizador = fabrica.newDocumentBuilder();
            analizador.setErrorHandler(new DefaultHandler());
            Document documento = analizador.parse(ruta.toFile());
            Element raiz = documento.getDocumentElement();

            if (!"catalogo".equals(raiz.getNodeName())) 
            {
                resultado.setErrorGeneral("la raíz debe ser catalogo");
                return resultado;
            }

            NodeList lista = raiz.getElementsByTagName("producto");
            List<Producto> validos = new ArrayList<>();
            Set<String> codigosVistos = new HashSet<>();

            for (int i = 0; i < lista.getLength(); i++) 
            {
                resultado.sumarEncontrado();

                Element productoElemento = (Element) lista.item(i);
                
                String codigo = leerCampo(productoElemento, "codigo");
                String nombre = leerCampo(productoElemento, "nombre");
                String precio = leerCampo(productoElemento, "precio");
                String stock = leerCampo(productoElemento, "stock");
                String categoriaId = leerCampo(productoElemento, "categoriaId");
                String proveedorId = leerCampo(productoElemento, "proveedorId");

                List<String> errores = validador.validar(codigo, nombre, precio, stock, categoriaId, proveedorId);
                
                String codigoMostrar;
                if (codigo != null && !codigo.trim().isEmpty()) 
                {
                    codigoMostrar = codigo;
                } 
                else 
                {
                    codigoMostrar = "sin código";
                }

                if (!errores.isEmpty()) 
                {
                    resultado.agregarIncidencia("Producto " + (i + 1) + " (" + codigoMostrar + "): " + String.join("; ", errores));
                } 
                else 
                {
                    String codigoNormalizado = ValidadorProducto.normalizarCodigo(codigo);
                    if (!codigosVistos.add(codigoNormalizado)) 
                    {
                        resultado.agregarIncidencia("Producto " + (i + 1) + " (" + codigoMostrar + "): código repetido en el fichero");
                    } 
                    else 
                    {
                        validos.add(validador.crear(codigo, nombre, precio, stock, categoriaId, proveedorId));
                    }
                }
            }
            for (Producto producto : validos) 
            {
                if (inventario.agregar(producto)) 
                {
                    resultado.sumarImportado();
                } 
                else 
                {
                    resultado.agregarIncidencia(producto.getCodigo() + ": ya existe en el inventario");
                }
            }

        } 
        catch (SAXException e) 
        {
            resultado.setErrorGeneral("el fichero contiene un XML mal formado o con DTD no permitida");
        } 
        catch (IOException e) 
        {
            resultado.setErrorGeneral("se ha producido un error de lectura en el fichero");
        } 
        catch (ParserConfigurationException e) 
        {
            resultado.setErrorGeneral("no se pudo configurar el analizador XML");
        }

        return resultado;
    }
    /**
     * Lee un campo de un producto XML como texto.
     * @return el valor como texto, o null si falta, es null o no es un valor simple
     */
    private String leerCampo(Element producto, String nombre) 
    {
        NodeList nodos = producto.getElementsByTagName(nombre);
        if (nodos.getLength() == 0) 
        {
            return null;
        }
        return nodos.item(0).getTextContent();
    }
}