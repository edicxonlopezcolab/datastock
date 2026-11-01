package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.ficheros.ExploradorRutas;
import es.iesisidra.datastock.ficheros.ExportadorJson;
import es.iesisidra.datastock.ficheros.ExportadorXml;
import es.iesisidra.datastock.ficheros.ImportadorJson;
import es.iesisidra.datastock.ficheros.ImportadorXml;
import es.iesisidra.datastock.ficheros.SerializadorInventario;
import es.iesisidra.datastock.jdbc.CategoriaDAO;
import es.iesisidra.datastock.jdbc.ComprobadorConexion;
import es.iesisidra.datastock.jdbc.ErroresSQL;
import es.iesisidra.datastock.jdbc.InfoConexion;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ResultadoImportacion;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación
 * y el procesamiento en métodos o clases independientes.</p>
 */
public class MenuPrincipal {
    private final Inventario inventario;
    private final ExploradorRutas explorador = new ExploradorRutas();
    private final ImportadorJson importadorJson = new ImportadorJson();
    private final ImportadorXml importadorXml = new ImportadorXml();
    private final ExportadorJson exportadorJson = new ExportadorJson();
    private final ExportadorXml exportadorXml = new ExportadorXml();
    private final SerializadorInventario serializador = new SerializadorInventario();
    private final ComprobadorConexion comprobador = new ComprobadorConexion();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    /** @param inventario colección común de productos de esta ejecución. */
    public MenuPrincipal(Inventario inventario) { this.inventario = inventario; }

    /** Ejecuta el menú principal hasta que el usuario selecciona salir. */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios (UT1)");
            System.out.println("2. Importar productos desde JSON (UT1)");
            System.out.println("3. Importar productos desde XML (UT1)");
            System.out.println("4. Consultar productos en memoria (UT1)");
            System.out.println("5. Exportar productos (UT1)");
            System.out.println("6. Guardar y recuperar inventario (UT1)");
            System.out.println("7. Gestionar base de datos MySQL (UT2)");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 7);
            switch (opcion) {
                case 1 -> explorarRuta();
                case 2 -> importarJson() ;
                case 3 -> importarXml();
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 7 -> menuMySQL();
                case 0 -> System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 2 - UT2", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
    }

    private void menuConsultas() {
        int opcion;
        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 3);
            switch (opcion) {
                case 1 -> listarProductos();
                case 2 -> buscarProductoPorCodigo();
                case 3 -> buscarProductosPorNombre();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuExportaciones() {
        int opcion;
        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> exportarJson();
                case 2 -> exportarXml();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuSerializacion() {
        int opcion;
        do {
            System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> guardarInventario();
                case 2 -> recuperarInventario();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuMySQL()
    {
        int opcion;
        do
        {
            System.out.println("\n---------- GESTIÓN MYSQL - UT2 ----------");
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 9);
            switch (opcion)
            {
                case 1 -> comprobarConexion();
                case 2 -> menuCategorias();
                case 3 -> pendiente("RF-JDBC-02 · Proveedores");
                case 4 -> pendiente("RF-JDBC-03 · Productos");
                case 5 -> pendiente("RF-JDBC-04 · Consultas");
                case 6 -> pendiente("RF-JDBC-05 · Movimientos");
                case 7 -> pendiente("RF-JDBC-06 · Historial");
                case 8 -> pendiente("RF-JDBC-07 · Importar de memoria");
                case 9 -> pendiente("RF-JDBC-08 · Transacciones");
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        }
        while (opcion != 0);
    }

        /**
     * Opción 7.1: comprueba la conexión con MySQL y muestra los datos
     * de la base de datos (RF-JDBC-01).
     */
    private void comprobarConexion()
    {
        System.out.println("\n---------- COMPROBAR CONEXIÓN ----------");

        ComprobadorConexion comprobador = new ComprobadorConexion();
        try
        {
            InfoConexion info = comprobador.comprobar();

            System.out.println(ColoresConsola.pintar("[OK] Conexión establecida correctamente.", ColoresConsola.VERDE));
            System.out.println("Base de datos: " + info.getBaseDatos());
            System.out.println("Gestor: " + info.getGestor());
            System.out.println("Versión: " + info.getVersion());
            System.out.println("Productos almacenados: " + info.getProductos());
        }
        catch (SQLException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + ErroresSQL.explicar(e), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void menuCategorias()
    {
        int opcion;
        do
        {
            System.out.println("\n---------- CATEGORÍAS ----------");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("5. Consultar categoría por id");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 5);
            switch (opcion)
            {
                case 1 -> altaCategoria();
                case 2 -> listarCategorias();
                case 3 -> pendiente("Modificar categoría");
                case 4 -> pendiente("Eliminar categoría");
                case 5 -> consultarCategoria();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        }
        while (opcion != 0);
    }

    private void listarCategorias()
    {
        System.out.println("\n---------- LISTADO DE CATEGORÍAS ----------");
        try
        {
            List<Categoria> categorias = categoriaDAO.listar();
            
            if (categorias.isEmpty())
            {
                System.out.println("No hay categorías registradas.");
            }
            else
            {
                for (Categoria c : categorias)
                {
                    System.out.println(c.getId() + " | " + c.getNombre());
                }
                System.out.println("Total: " + categorias.size());
            }
        }
        catch (SQLException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + ErroresSQL.explicar(e), ColoresConsola.ROJO));
        }
        
        LectorConsola.pausar();
    }

    private void consultarCategoria() 
    {
        System.out.println("\n---------- CONSULTAR CATEGORÍA ----------");   
        int id = LectorConsola.leerEntero("Id de la categoría: ");
        if (id <= 0) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el id debe ser un entero positivo", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }
        try 
        {
            Categoria c = categoriaDAO.buscarPorId(id);
            
            if (c == null) 
            {
                System.out.println("No existe ninguna categoría con id " + id);
            } 
            else 
            {
                System.out.println(c.getId() + " | " + c.getNombre());
            }
        } 
        catch (SQLException e) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + ErroresSQL.explicar(e), ColoresConsola.ROJO));
        }
        
        LectorConsola.pausar();
    }

    private void altaCategoria() 
    {
        System.out.println("\n---------- ALTA DE CATEGORÍA ----------");
        
        int id = LectorConsola.leerEntero("Id de la categoría: ");
        if (id <= 0) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el id debe ser un entero positivo", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }
        String nombre = LectorConsola.leerTexto("Nombre de la categoría: ").trim();
        if (nombre.isEmpty()) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el nombre no puede estar vacío", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }
        
        if (nombre.length() > 60) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el nombre no puede superar los 60 caracteres", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }
        
        try 
        {
            categoriaDAO.insertar(new Categoria(id, nombre));
            System.out.println(ColoresConsola.pintar("[OK] Categoría creada.", ColoresConsola.VERDE));
        } 
        catch (SQLException e) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + ErroresSQL.explicar(e), ColoresConsola.ROJO));
        }
        
        LectorConsola.pausar();
    }

    /** Sustituir las llamadas por servicios propios a medida que se implementen los RF. */
    private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar("[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    }

    private void explorarRuta() 
    {
        System.out.println("\n---------- EXPLORAR RUTA ----------");
        String texto = LectorConsola.leerTexto("Digite la Ruta: ");
        explorador.explorar(texto);
        LectorConsola.pausar();
    }

    private void importarJson()
    {
        System.out.println("\n---------- IMPORTAR JSON ----------");
        String texto = LectorConsola.leerTexto("Digite la Ruta: ");
        ResultadoImportacion resultado = importadorJson.importar(texto, inventario);
        mostrarResultado(resultado);
        LectorConsola.pausar();
    }

    private void importarXml()
    {
        System.out.println("\n---------- IMPORTAR XML ----------");
        String texto = LectorConsola.leerTexto("Digite la Ruta: ");
        ResultadoImportacion resultado = importadorXml.importar(texto, inventario);
        mostrarResultado(resultado);
        LectorConsola.pausar();
    }

    private void mostrarProducto(Producto p) 
    {
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " € | Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId() + " | Proveedor: " + p.getProveedorId());
    }

    private void listarProductos() 
    {
        System.out.println("\n---------- LISTAR PRODUCTOS ----------");
        List<Producto> productos = inventario.listarOrdenado();
        
        if (productos.isEmpty()) 
        {
            System.out.println("No hay productos en el inventario.");
        } 
        else 
        {
            mostrarLista(productos);
        }
        LectorConsola.pausar();
    }

    private void buscarProductoPorCodigo() 
    {
        System.out.println("\n---------- BUSCAR PRODUCTOS POR CÓDIGO ----------");
        String codigo = LectorConsola.leerTexto("Introduce el código del producto: ").trim();

        if (codigo.isEmpty()) 
        {
            System.out.println("El código no puede estar vacío.");
        } 
        else 
        {
            Producto p = inventario.buscarPorCodigo(codigo);
            if (p == null) 
            {
                System.out.println("No existe ningún producto con ese código.");
            } 
            else 
            {
                mostrarProducto(p);
            }
        }
        LectorConsola.pausar();
    }

    private void buscarProductosPorNombre() 
    {
        System.out.println("\n---------- BUSCAR PRODUCTOS POR NOMBRE ----------");
        String texto = LectorConsola.leerTexto("Introduce el texto a buscar en el nombre: ").trim();

        if (texto.isEmpty()) 
        {
            System.out.println("El texto no puede estar vacío.");
        } 
        else 
        {
            List<Producto> resultados = inventario.buscarPorNombre(texto);
            
            if (resultados.isEmpty()) 
            {
                System.out.println("No hay coincidencias.");
            } 
            else 
            {
                mostrarLista(resultados);
            }
        }
        LectorConsola.pausar();
    }

    private void mostrarLista(List<Producto> lista) 
    {
        boolean primero = true;
        for (Producto p : lista) 
        {
            if (!primero) 
            {
                System.out.println("----------------------------------------");
            }
            mostrarProducto(p);
            primero = false;
        }
        System.out.println("\nTotal: " + lista.size() + " productos");
    }

    private void mostrarResultado(ResultadoImportacion resultado)
    {
        if (resultado.hayErrorGeneral())
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + resultado.getErrorGeneral(), ColoresConsola.ROJO));
        }
        else
        {
            System.out.println("Encontrados: " + resultado.getEncontrados());
            System.out.println("Importados: " + resultado.getImportados());
            System.out.println("Rechazados: " + resultado.getRechazados());

            if (!resultado.getIncidencias().isEmpty())
            {
                System.out.println("Detalle de incidencias:");
                for (String incidencia : resultado.getIncidencias())
                {
                    System.out.println("  - " + incidencia);
                }
            }
            System.out.println(ColoresConsola.pintar("[OK] Importación finalizada.", ColoresConsola.VERDE));
        }
    }

    private Path pedirRutaDestino()
    {
        String texto = LectorConsola.leerTexto("Ruta del fichero de destino: ");
        if (texto == null || texto.trim().isEmpty()) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la ruta está vacía",ColoresConsola.ROJO));    
            return null;
        }
        Path destino;
        try 
        {
            destino = Paths.get(texto.trim());
        } 
        catch (InvalidPathException e) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la ruta no es válida",ColoresConsola.ROJO));    
            return null;
        }
        Path carpeta = destino.toAbsolutePath().getParent();
        if (carpeta == null || !Files.isDirectory(carpeta)) 
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la carpeta de destino no existe",ColoresConsola.ROJO));    
            return null;
        }
        if (Files.exists(destino)) 
        {
            if (Files.isDirectory(destino)) 
            {
                System.out.println(ColoresConsola.pintar("[ERROR] la ruta apunta a un directorio, no a un fichero", ColoresConsola.ROJO));
                return null;
            }
            if(!LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribirlo?"))
            {
                System.out.println(ColoresConsola.pintar("Operación cancelada.", ColoresConsola.AMARILLO));
                return null;
            }    
        }
        return destino;
    }

    private void exportarJson()
    {
        System.out.println("\n---------- EXPORTAR JSON ----------");

        if (inventario.getProductos().isEmpty())
        {
            System.out.println("No hay productos para exportar.");
            LectorConsola.pausar();
            return;
        }

        Path destino = pedirRutaDestino();
        if (destino == null)
        {
            LectorConsola.pausar();
            return;
        }

        try
        {
            int total = exportadorJson.exportar(inventario.listarOrdenado(), destino);
            System.out.println(ColoresConsola.pintar("[OK] " + total + " productos exportados a JSON en " + destino.toAbsolutePath(),
                ColoresConsola.VERDE));
        }
        catch (IOException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] no se pudo recuperar el inventario: " + e.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void exportarXml()
    {
        System.out.println("\n---------- EXPORTAR XML ----------");

        if (inventario.getProductos().isEmpty())
        {
            System.out.println("No hay productos para exportar.");
            LectorConsola.pausar();
            return;
        }

        Path destino = pedirRutaDestino();
        if (destino == null)
        {
            LectorConsola.pausar();
            return;
        }

        try
        {
            int total = exportadorXml.exportar(inventario.listarOrdenado(), destino);
            System.out.println(ColoresConsola.pintar("[OK] " + total + " productos exportados a XML en " + destino.toAbsolutePath(),
                ColoresConsola.VERDE));
        }
        catch (IOException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void guardarInventario()
    {
        System.out.println("\n---------- GUARDAR INVENTARIO ----------");

        if (inventario.getProductos().isEmpty())
        {
            System.out.println("No hay productos para guardar.");
            LectorConsola.pausar();
            return;
        }

        Path destino = pedirRutaDestino();
        if (destino == null)
        {
            LectorConsola.pausar();
            return;
        }

        try
        {
            int total = serializador.guardar(inventario.listarOrdenado(), destino);
            System.out.println(ColoresConsola.pintar(
                "[OK] " + total + " productos guardados en " + destino.toAbsolutePath(),
                ColoresConsola.VERDE));
        }
        catch (IOException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void recuperarInventario()
    {
        System.out.println("\n---------- RECUPERAR INVENTARIO ----------");

        String texto = LectorConsola.leerTexto("Ruta del fichero .dat: ");
        if (texto == null || texto.trim().isEmpty())
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la ruta está vacía", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        Path origen;
        try
        {
            origen = Paths.get(texto.trim());
        }
        catch (InvalidPathException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la ruta no es válida", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        if (!Files.exists(origen) || !Files.isRegularFile(origen))
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el fichero no existe o no es un fichero regular", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        List<Producto> recuperados;
        try
        {
            recuperados = serializador.recuperar(origen);
        }
        catch (IOException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] no se pudo recuperar el inventario: el fichero está dañado o no es un inventario de DataStock", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return; 
        }

        if (!inventario.getProductos().isEmpty())
        {
            if (!LectorConsola.confirmar("El inventario actual tiene " + inventario.getProductos().size() + " productos. ¿Sustituirlo por los " + recuperados.size() + " del fichero?"))
            {
                System.out.println(ColoresConsola.pintar("Recuperación cancelada.", ColoresConsola.AMARILLO));
                LectorConsola.pausar();
                return;
            }
        }

        inventario.sustituir(recuperados);
        System.out.println(ColoresConsola.pintar("[OK] " + recuperados.size() + " productos recuperados desde " + origen.toAbsolutePath(), ColoresConsola.VERDE));
        LectorConsola.pausar();
    }

}

