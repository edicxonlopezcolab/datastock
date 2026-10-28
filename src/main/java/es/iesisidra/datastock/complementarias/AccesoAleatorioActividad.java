package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import es.iesisidra.datastock.ficheros.SerializadorInventario;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Acceso aleatorio a un fichero binario de stock con registros de longitud fija (RF-FIC-08).
 * <p>Actividad independiente del menú principal. Los productos se cargan desde un
 * inventario guardado con RF-FIC-06 (.dat). El fichero stock.bin es una copia
 * independiente: modificarlo no cambia el inventario ni el .dat.</p>
 */
public final class AccesoAleatorioActividad
{
    /** Caracteres del código: 12 caracteres Java = 24 bytes. */
    private static final int LONGITUD_CODIGO = 12;
    /** Tamaño de cada registro: 24 (código) + 8 (precio) + 4 (stock). */
    private static final int TAMANIO_REGISTRO = 36;
    /** Posición del campo stock dentro de cada registro. */
    private static final int DESPLAZAMIENTO_STOCK = 32;

    private List<Producto> productos = new ArrayList<>();
    private Path rutaBin;

    private AccesoAleatorioActividad() { }

    /** Punto de entrada de la actividad. */
    public static void main(String[] args)
    {
        new AccesoAleatorioActividad().iniciar();
    }

    private void iniciar()
    {
        System.out.println("\n===== ACCESO ALEATORIO =====");
        cargarProductos();
        rutaBin = pedirRuta("Fichero binario de stock (ej. datos/salidas/stock.bin): ");
        if (rutaBin == null)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] hace falta la ruta del fichero binario", ColoresConsola.ROJO));
            return;
        }

        int opcion;
        do
        {
            System.out.println("\n1. Generar stock.bin | 2. Mostrar todos | 3. Consultar posición | 4. Modificar stock | 0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);
            try
            {
                switch (opcion)
                {
                    case 1 -> generar();
                    case 2 -> mostrarTodos();
                    case 3 -> consultarPosicion();
                    case 4 -> modificarStock();
                    case 0 -> System.out.println("Fin de la actividad.");
                    default -> throw new IllegalStateException("Opción inesperada");
                }
            }
            catch (IOException e)
            {
                System.out.println(ColoresConsola.pintar("[ERROR] " + e.getMessage(), ColoresConsola.ROJO));
            }
        }
        while (opcion != 0);
    }

    /** Carga los productos desde un .dat de RF-FIC-06, ordenados por código. */
    private void cargarProductos()
    {
        Path dat = pedirRuta("Inventario .dat de origen (Intro para no cargar): ");
        if (dat == null)
        {
            System.out.println("No se cargan productos: no se podrá generar stock.bin.");
            return;
        }
        try
        {
            productos = new ArrayList<>(new SerializadorInventario().recuperar(dat));
            productos.sort(Comparator.comparing(Producto::getCodigo));
            System.out.println(ColoresConsola.pintar("[OK] " + productos.size() + " productos cargados en memoria.", ColoresConsola.VERDE));
        }
        catch (IOException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] no se pudo cargar el inventario: el fichero está dañado o no es un inventario de DataStock", ColoresConsola.ROJO));
        }
    }

    /** Pide una ruta; devuelve null si está vacía o no es válida. */
    private Path pedirRuta(String mensaje)
    {
        String texto = LectorConsola.leerTexto(mensaje).trim();
        if (texto.isEmpty())
        {
            return null;
        }
        try
        {
            return Paths.get(texto);
        }
        catch (InvalidPathException e)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] la ruta no es válida", ColoresConsola.ROJO));
            return null;
        }
    }

    /**
     * Devuelve cuántos registros tiene el fichero, comprobando que su tamaño es correcto.
     * @throws IOException si el tamaño no es múltiplo de 36
     */
    private long numeroRegistros(RandomAccessFile fichero) throws IOException
    {
        long tamanio = fichero.length();
        if (tamanio % TAMANIO_REGISTRO != 0)
        {
            throw new IOException("fichero inválido: " + tamanio + " bytes no es múltiplo de " + TAMANIO_REGISTRO);
        }
        return tamanio / TAMANIO_REGISTRO;
    }

    /** Genera stock.bin a partir de los productos cargados, truncando el fichero antes de escribir. */
    private void generar() throws IOException
    {
        if (productos.isEmpty())
        {
            System.out.println(ColoresConsola.pintar("[ERROR] no hay productos en memoria: no se genera el fichero", ColoresConsola.ROJO));
            return;
        }
        if (Files.exists(rutaBin) && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribirlo?"))
        {
            System.out.println(ColoresConsola.pintar("Generación cancelada.", ColoresConsola.AMARILLO));
            return;
        }

        try (RandomAccessFile fichero = new RandomAccessFile(rutaBin.toFile(), "rw"))
        {
            fichero.setLength(0);
            for (Producto p : productos)
            {
                fichero.writeChars(rellenar(p.getCodigo()));
                fichero.writeDouble(p.getPrecio().doubleValue());
                fichero.writeInt(p.getStock());
            }
            System.out.println(ColoresConsola.pintar("[OK] Registros: " + productos.size() + " | Tamaño: " + fichero.length() + " bytes", ColoresConsola.VERDE));
        }
    }

    /** Muestra todos los registros leyendo el fichero de forma secuencial. */
    private void mostrarTodos() throws IOException
    {
        if (!Files.isRegularFile(rutaBin))
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el fichero no existe; genéralo primero", ColoresConsola.ROJO));
            return;
        }
        try (RandomAccessFile fichero = new RandomAccessFile(rutaBin.toFile(), "r"))
        {
            long n = numeroRegistros(fichero);
            if (n == 0)
            {
                System.out.println("El fichero está vacío.");
                return;
            }
            for (long i = 0; i < n; i++)
            {
                String codigo = leerCodigo(fichero);
                double precio = fichero.readDouble();
                int stock = fichero.readInt();
                System.out.println(i + " | " + codigo + " | " + formatearPrecio(precio) + " € | stock " + stock);
            }
            System.out.println("Registros: " + n + " | Tamaño: " + fichero.length() + " bytes");
        }
    }

    /** Lee un registro saltando directamente a su posición con seek. */
    private void consultarPosicion() throws IOException
    {
        if (!Files.isRegularFile(rutaBin))
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el fichero no existe; genéralo primero", ColoresConsola.ROJO));
            return;
        }
        try (RandomAccessFile fichero = new RandomAccessFile(rutaBin.toFile(), "r"))
        {
            long n = numeroRegistros(fichero);
            int pos = pedirPosicion(n);
            if (pos < 0)
            {
                return;
            }
            fichero.seek((long) pos * TAMANIO_REGISTRO);
            String codigo = leerCodigo(fichero);
            double precio = fichero.readDouble();
            int stock = fichero.readInt();
            System.out.println(codigo + " | " + formatearPrecio(precio) + " € | stock " + stock);
        }
    }

    /** Modifica solo el campo stock de un registro, sin reescribir el resto. */
    private void modificarStock() throws IOException
    {
        if (!Files.isRegularFile(rutaBin))
        {
            System.out.println(ColoresConsola.pintar("[ERROR] el fichero no existe; genéralo primero", ColoresConsola.ROJO));
            return;
        }
        try (RandomAccessFile fichero = new RandomAccessFile(rutaBin.toFile(), "rw"))
        {
            long n = numeroRegistros(fichero);
            int pos = pedirPosicion(n);
            if (pos < 0)
            {
                return;
            }

            long posicionStock = (long) pos * TAMANIO_REGISTRO + DESPLAZAMIENTO_STOCK;
            fichero.seek(posicionStock);
            int anterior = fichero.readInt();

            int nuevo = LectorConsola.leerEntero("Nuevo stock: ");
            if (nuevo < 0)
            {
                System.out.println(ColoresConsola.pintar("[ERROR] el stock no puede ser negativo", ColoresConsola.ROJO));
                return;
            }

            fichero.seek(posicionStock);
            fichero.writeInt(nuevo);
            System.out.println(ColoresConsola.pintar("[OK] Actualización correcta: stock " + anterior + " → " + nuevo, ColoresConsola.VERDE));
        }
    }

    // ===== Auxiliares =====

    /**
     * Pide una posición entre 0 y n-1.
     * @return la posición, o -1 si el fichero está vacío o la posición no es válida
     */
    private int pedirPosicion(long n)
    {
        if (n == 0)
        {
            System.out.println("El fichero está vacío.");
            return -1;
        }
        int pos = LectorConsola.leerEntero("Posición (0-" + (n - 1) + "): ");
        if (pos < 0 || pos >= n)
        {
            System.out.println(ColoresConsola.pintar("[ERROR] posición fuera de rango", ColoresConsola.ROJO));
            return -1;
        }
        return pos;
    }

    /** Rellena el código con espacios hasta 12 caracteres (o lo recorta si fuera más largo). */
    private String rellenar(String codigo)
    {
        String relleno = String.format("%-" + LONGITUD_CODIGO + "s", codigo);
        return relleno.substring(0, LONGITUD_CODIGO);
    }

    /** Lee los 12 caracteres del código y quita los espacios de relleno. */
    private String leerCodigo(RandomAccessFile fichero) throws IOException
    {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < LONGITUD_CODIGO; i++)
        {
            codigo.append(fichero.readChar());
        }
        return codigo.toString().trim();
    }

    /** Muestra el precio con dos decimales y punto decimal. */
    private String formatearPrecio(double precio)
    {
        return String.format(Locale.ROOT, "%.2f", precio);
    }
}