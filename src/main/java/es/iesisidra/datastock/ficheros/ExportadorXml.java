package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import es.iesisidra.datastock.modelo.Producto;

/**
 * Exporta productos a un catálogo XML mediante DOM (RF-FIC-05).
 * <p>No imprime nada por consola; los errores se lanzan como IOException
 * para que el menú los muestre.</p>
 */
public class ExportadorXml
{
    /**
     * Escribe los productos en un XML con raíz catalogo, indentado y en UTF-8.
     * @param productos productos a exportar, ya ordenados
     * @param destino fichero de destino
     * @return número de productos exportados
     * @throws IOException si no se puede generar o escribir el fichero
     */
    public int exportar(List<Producto> productos, Path destino) throws IOException
    {
        try
        {
            Document documento = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();

            Element catalogo = documento.createElement("catalogo");
            documento.appendChild(catalogo);

            for (Producto p : productos)
            {
                Element elementoProducto = documento.createElement("producto");
                catalogo.appendChild(elementoProducto);

                agregarCampo(documento, elementoProducto, "codigo",      p.getCodigo());
                agregarCampo(documento, elementoProducto, "nombre",      p.getNombre());
                agregarCampo(documento, elementoProducto, "precio",      p.getPrecio().toPlainString());
                agregarCampo(documento, elementoProducto, "stock",       String.valueOf(p.getStock()));
                agregarCampo(documento, elementoProducto, "categoriaId", String.valueOf(p.getCategoriaId()));
                agregarCampo(documento, elementoProducto, "proveedorId", String.valueOf(p.getProveedorId()));
            }

            Transformer transformador = TransformerFactory.newInstance().newTransformer();
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            try (Writer escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8))
            {
                transformador.transform(new DOMSource(documento), new StreamResult(escritor));
            }
        }
        catch (ParserConfigurationException | TransformerException e)
        {
            throw new IOException("no se pudo generar el XML: " + e.getMessage(), e);
        }
        return productos.size();
    }

    /** Añade al elemento padre un hijo con el nombre y el texto indicados. */
    private void agregarCampo(Document documento, Element padre, String nombre, String valor)
    {
        Element campo = documento.createElement(nombre);
        campo.setTextContent(valor);
        padre.appendChild(campo);
    }
}