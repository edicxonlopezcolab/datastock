package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;

public class ImportadorXml 
{
    public ResultadoImportacion importar(String textoRuta, Inventario inventario) 
    {

        ResultadoImportacion resultado = new ResultadoImportacion();

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
            Document documento = analizador.parse(ruta.toFile());
            Element raiz = documento.getDocumentElement();

            if (!"catalogo".equals(raiz.getNodeName())) 
            {
                resultado.setErrorGeneral("la raíz debe ser catalogo");
                return resultado;
            }

            NodeList lista = raiz.getElementsByTagName("producto");
            for (int i = 0; i < lista.getLength(); i++) 
            {
                resultado.sumarEncontrado();
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
}