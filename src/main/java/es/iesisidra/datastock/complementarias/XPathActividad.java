package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.validacion.ValidadorProducto;

/**
 * Consultas XPath sobre un catálogo XML (RF-FIC-07).
 * <p>Actividad independiente del menú principal: consulta el fichero
 * directamente, sin importar sus productos al inventario.</p>
 */
public final class XPathActividad
{
    private final XPath xpath = XPathFactory.newInstance().newXPath();
    private Document documento;

    private XPathActividad() { }

    /** Punto de entrada: abre un XML y permite consultarlo varias veces. */
    public static void main(String[] args)
    {
        new XPathActividad().iniciar();
    }

    private void iniciar()
    {
        System.out.println("\n===== CONSULTAS XPATH =====");
        if (!abrirDocumento())
        {
            return;
        }

        int opcion;
        do
        {
            System.out.println("\n1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);
            try
            {
                switch (opcion)
                {
                    case 1 -> consultarPorCodigo();
                    case 2 -> consultarPorCategoria();
                    case 3 -> consultarPrecioSuperior();
                    case 4 -> contarProductos();
                    case 0 -> System.out.println("Fin de las consultas.");
                    default -> throw new IllegalStateException("Opción inesperada");
                }
            }
            catch (XPathExpressionException e)
            {
                System.out.println(ColoresConsola.pintar("[ERROR] no se pudo ejecutar la consulta: " + e.getMessage(), ColoresConsola.ROJO));
            }
        }
        while (opcion != 0);
    }

    /**
     * Pide la ruta y carga el XML con protección frente a XXE.
     * @return true si el documento se ha cargado y su raíz es catalogo
     */
    private boolean abrirDocumento()
    {
        String texto = LectorConsola.leerTexto("Fichero XML: ");
        Path ruta;
        try
        {
            ruta = Paths.get(texto.trim());
        }
        catch (InvalidPathException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la ruta no es válida", ColoresConsola.ROJO));
            return false;
        }
        if (!Files.isRegularFile(ruta))
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el fichero no existe", ColoresConsola.ROJO));
            return false;
        }
        try
        {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);
            DocumentBuilder analizador = fabrica.newDocumentBuilder();
            analizador.setErrorHandler(new DefaultHandler());
            documento = analizador.parse(ruta.toFile());
        }
        catch (SAXException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el XML está mal formado o contiene DOCTYPE (no permitido)", ColoresConsola.ROJO));
            return false;
        }
        catch (IOException | ParserConfigurationException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] no se pudo leer el fichero", ColoresConsola.ROJO));
            return false;
        }
        if (!"catalogo".equals(documento.getDocumentElement().getNodeName()))
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la raíz debe ser catalogo", ColoresConsola.ROJO));
            return false;
        }
        System.out.println(ColoresConsola.pintar("[OK] Documento cargado: " + ruta, ColoresConsola.VERDE));
        return true;
    }


    private void consultarPorCodigo() throws XPathExpressionException
    {
        String texto = LectorConsola.leerTexto("Introduce el código a buscar: ").trim();
        if (texto.isEmpty())
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el código no puede estar vacío", ColoresConsola.ROJO));
            return;
        }

        String codigoBuscado = ValidadorProducto.normalizarCodigo(texto);
        NodeList todos = (NodeList) xpath.evaluate("/catalogo/producto", documento, XPathConstants.NODESET);
        
        int encontrados = 0;
        for (int i = 0; i < todos.getLength(); i++)
        {
            Element p = (Element) todos.item(i);
            String codigoActual = ValidadorProducto.normalizarCodigo(texto(p, "codigo"));
            
            // Comparamos en Java, no en el XPath
            if (codigoActual.equals(codigoBuscado))
            {
                mostrar(p);
                encontrados++;
            }
        }

        if (encontrados == 0)
        {
            System.out.println("No se ha encontrado ningún producto con ese código.");
        }
        else
        {
            System.out.println("Total encontrados: " + encontrados);
        }
    }

    private void consultarPorCategoria() throws XPathExpressionException
    {
        String texto = LectorConsola.leerTexto("Introduce el ID de la categoría: ").trim();
        if (texto.isEmpty())
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la categoría no puede estar vacía", ColoresConsola.ROJO));
            return;
        }

        int categoria;
        try
        {
            categoria = Integer.parseInt(texto);
        }
        catch (NumberFormatException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la categoría debe ser un número entero válido", ColoresConsola.ROJO));
            return;
        }

        if (categoria <= 0)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la categoría debe ser mayor que 0", ColoresConsola.ROJO));
            return;
        }

        String expresion = "/catalogo/producto[categoriaId=" + categoria + "]";
        NodeList resultado = (NodeList) xpath.evaluate(expresion, documento, XPathConstants.NODESET);
        
        mostrarResultados(resultado);
    }

    private void consultarPrecioSuperior() throws XPathExpressionException
    {
        String texto = LectorConsola.leerTexto("Introduce el precio mínimo (ej. 10.50): ").trim();
        if (texto.isEmpty())
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el precio no puede estar vacío", ColoresConsola.ROJO));
            return;
        }

        BigDecimal precio;
        try
        {
            precio = new BigDecimal(texto);
        }
        catch (NumberFormatException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el precio debe ser un número válido", ColoresConsola.ROJO));
            return;
        }

        if (precio.compareTo(BigDecimal.ZERO) < 0)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el precio no puede ser negativo", ColoresConsola.ROJO));
            return;
        }

        String expresion = "/catalogo/producto[precio > " + precio.toPlainString() + "]";
        NodeList resultado = (NodeList) xpath.evaluate(expresion, documento, XPathConstants.NODESET);
        
        mostrarResultados(resultado);
    }

    private void contarProductos() throws XPathExpressionException
    {
        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", documento, XPathConstants.NUMBER);
        System.out.println("Productos en el documento: " + total.intValue());
    }

    /** Muestra los productos encontrados y el total, o un aviso si no hay ninguno. */
    private void mostrarResultados(NodeList resultado)
    {
        if (resultado.getLength() == 0)
        {
            System.out.println("Sin coincidencias.");
            return;
        }

        for (int i = 0; i < resultado.getLength(); i++)
        {
            mostrar((Element) resultado.item(i));
        }
        System.out.println("Total encontrados: " + resultado.getLength());
    }

    /** Muestra un producto en una línea: código, nombre y precio. */
    private void mostrar(Element p)
    {
        System.out.println(texto(p, "codigo") + " | " + texto(p, "nombre") + " | " + texto(p, "precio") + " €");
    }

    /** Devuelve el texto de una etiqueta hija, o cadena vacía si no existe. */
    private String texto(Element padre, String etiqueta)
    {
        NodeList nodos = padre.getElementsByTagName(etiqueta);
        return nodos.getLength() == 0 ? "" : nodos.item(0).getTextContent().trim();
    }
}