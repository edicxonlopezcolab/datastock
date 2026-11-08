# DataStock – UT2

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola · **Base de datos:** MySQL 8 (JDBC)

**Autor:** Gabriel López · [GitHub](https://github.com/edicxonlopezcolab)

## Estado del desarrollo

| Requisito | Descripción | Estado |
|---|---|---|
| RF-FIC-01 a RF-FIC-08 | Ficheros, JSON, XML, serialización, XPath y acceso aleatorio (UT1) | ✅ Completado |
| RF-JDBC-01 | Conectar y comprobar el esquema (7.1) | ✅ Completado |
| RF-JDBC-02 | Gestionar categorías y proveedores (7.2 y 7.3) | ✅ Completado |
| RF-JDBC-03 | CRUD de productos (7.4) | ✅ Completado |
| RF-JDBC-04 | Consultas con ResultSet (7.5) | ✅ Completado |
| RF-JDBC-05 | Movimientos con procedimiento almacenado (7.6) | ✅ Completado |
| RF-JDBC-06 | Historial de movimientos (7.7) | ✅ Completado |
| RF-JDBC-07 | Importación de memoria a MySQL en una transacción (7.8) | ✅ Completado |
| RF-JDBC-08 | Demostración de commit y rollback (7.9) | ✅ Completado |
| SQLite | `DemoSQLite`, gestor embebido (independiente del menú) | ✅ Completado |

Las pruebas se documentan en `docs/DataStock_UT1_Registro_Pruebas.docx` (52, todas superadas) y
`docs/DataStock_UT2_Registro_Pruebas.docx` (37, todas superadas). Las salidas de cada una están
en `evidencias/`.

## 1. ¿Qué es este proyecto?

DataStock es una práctica **individual e integradora** que evoluciona durante tres unidades:

- **UT1 – Ficheros:** importar JSON/XML, consultar en memoria, exportar, serializar y dos actividades independientes (XPath y `RandomAccessFile`).
- **UT2 – JDBC:** incorporar MySQL: categorías, proveedores, productos, movimientos de stock con un procedimiento almacenado, importación en una transacción y una actividad comparativa con SQLite.
- **UT3 – Componentes:** reorganizar el acceso a datos en componentes reutilizables.

Esta versión corresponde a la entrega de la UT2 (etiqueta `UT2-v1.0`). La UT1 sigue funcionando sin cambios (etiqueta `ut1-entrega`).

## 2. Requisitos previos

- JDK **21** instalado y seleccionado en el IDE (`java -version`).
- Maven instalado, o un IDE con integración Maven.
- **MySQL 8.0.16 o posterior** (probado con 8.0.46) y, opcionalmente, MySQL Workbench.
- Conexión a Internet la primera vez, para que Maven descargue las dependencias:

| Dependencia | Versión | Uso |
|---|---|---|
| `com.google.code.gson:gson` | 2.11.0 | JSON (UT1) |
| `com.mysql:mysql-connector-j` | **9.3.0** | Conector JDBC de MySQL |
| `org.xerial:sqlite-jdbc` | 3.45.1.0 | Actividad `DemoSQLite` |

- Consola con UTF-8 para ver tildes y el símbolo € (en Windows: `chcp 65001` antes de `mvn exec:java`).

## 3. Preparar la base de datos MySQL

El esquema se crea con el script `material_referencia/DataStock_BD_MySQL.sql`, que **no se ha modificado**:
crea la base `datastock`, las tablas `categoria`, `proveedor`, `producto` y `movimiento_stock` con todas sus
restricciones, y el procedimiento `registrar_movimiento`. Es idempotente: puede ejecutarse varias veces sin borrar datos.

1. Ejecuta el script con MySQL Workbench (*File → Open SQL Script* y ejecutar) o desde el cliente `mysql`:

   ```sql
   SOURCE material_referencia/DataStock_BD_MySQL.sql;
   ```

2. Comprueba que existe todo lo necesario:

   ```sql
   USE datastock;
   SHOW TABLES;                                    -- categoria, movimiento_stock, producto, proveedor
   SHOW PROCEDURE STATUS WHERE Db = 'datastock';   -- registrar_movimiento
   ```

3. (Opcional) Crea un usuario propio en lugar de usar `root`:

   ```sql
   CREATE USER 'datastock'@'localhost' IDENTIFIED BY 'una_clave_segura';
   GRANT ALL PRIVILEGES ON datastock.* TO 'datastock'@'localhost';
   ```

El script **no inserta datos**. Las categorías y los proveedores se dan de alta desde el menú (7.2 y 7.3)
antes de guardar productos. No deben eliminarse ni modificarse las restricciones del script.

## 4. Configurar la conexión JDBC sin publicar contraseñas

Los datos de conexión se leen de `src/main/resources/db.properties`, un fichero **local excluido de Git**
(está en `.gitignore`). El repositorio solo incluye la plantilla `db.properties.example`.

1. Copia la plantilla:

   ```powershell
   Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties
   ```

   En Linux: `cp src/main/resources/db.properties.example src/main/resources/db.properties`.

2. Edita `db.properties` con tus datos:

   ```properties
   jdbc.url=jdbc:mysql://localhost:3306/datastock?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid
   jdbc.user=datastock
   jdbc.password=pon_aqui_tu_clave
   ```

3. Comprueba la conexión desde la aplicación: opción **7 → 1**. Si todo va bien muestra la base, el gestor, su versión y el número de productos.

Si algo falla, DataStock informa con un mensaje comprensible, sin mostrar la contraseña ni la traza, y sigue funcionando:

| Situación | Mensaje |
|---|---|
| Servidor detenido | no se puede conectar con el servidor MySQL; comprueba que está arrancado |
| Usuario o contraseña incorrectos | usuario o contraseña incorrectos |
| Esquema inexistente | la base de datos datastock no existe; ejecuta el script SQL |
| Falta `db.properties` | No se encuentra db.properties (copia db.properties.example) |

## 5. Cómo abrir y ejecutar

1. Clona el repositorio y abre **la carpeta que contiene `pom.xml`** como proyecto Maven en IntelliJ IDEA, Eclipse o VS Code.
2. Prepara MySQL (apartado 3) y la configuración (apartado 4).
3. En una terminal situada en la raíz:

```bash
mvn clean compile
mvn exec:java
```

Para ejecutar las actividades complementarias, independientes del menú:

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

> En PowerShell (Windows), el parámetro va entre comillas, porque PowerShell lo parte por el punto:
> `mvn exec:java "-Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite"`

**`DemoSQLite`** no usa MySQL ni el menú. Crea `datos/sqlite/productos_demo.db` (el directorio se crea desde Java si no existe),
la tabla `producto_demo`, inserta `SQL-001` y `SQL-002` con `INSERT OR IGNORE` y muestra las filas. Puede ejecutarse varias veces
sin duplicar registros. El fichero `.db` es un resultado de la ejecución y está excluido de Git (`datos/sqlite/*.db`).

La actividad de acceso aleatorio pide primero un inventario `.dat` guardado con la opción 6.1 del menú (por ejemplo `datos/pruebas/rf08/cuatro.dat`) y después la ruta del fichero binario de stock.

> Si el IDE muestra caracteres de control ANSI, desactiva el color al ejecutar Java con `-Ddatastock.color=false`, o configura la variable de entorno `NO_COLOR=1`.

## 6. Organización del proyecto

```text
DataStock_UT1_Proyecto_Inicial/
├── pom.xml
├── README.md
├── .gitignore
├── datos/
│   ├── ejemplos/              # JSON/XML para comenzar las pruebas
│   ├── pruebas/               # Ficheros de prueba propios (rf01 a rf08 y jdbc07)
│   ├── sqlite/                # Base SQLite generada por DemoSQLite (no se sube)
│   └── salidas/               # Exportaciones y binarios generados (no se suben)
├── docs/                      # Registros de pruebas UT1 y UT2 (Word y PDF)
├── evidencias/                # Salidas de cada prueba (rfXX/, jdbcXX/ y sqlite/)
├── material_referencia/       # Script SQL y diagrama entidad-relación
└── src/main/
    ├── resources/
    │   ├── db.properties.example     # Plantilla de configuración (se sube)
    │   └── db.properties             # Configuración local (NO se sube)
    └── java/es/iesisidra/datastock/
        ├── App.java                         # Punto de entrada
        ├── config/
        │   └── ConexionBD.java              # Abre conexiones MySQL desde db.properties
        ├── modelo/                          # Producto, Categoria, Proveedor, MovimientoStock
        ├── servicio/                        # Inventario en memoria y ResultadoImportacion
        ├── validacion/
        │   └── ValidadorProducto.java       # Reglas comunes (JSON, XML, .dat y MySQL)
        ├── ficheros/                        # UT1: exploración, importadores, exportadores, serialización
        ├── jdbc/                            # UT2: acceso a MySQL, sin consola
        │   ├── ComprobadorConexion.java     # RF-JDBC-01
        │   ├── InfoConexion.java
        │   ├── ErroresSQL.java              # Traduce los errores de MySQL a mensajes
        │   ├── CategoriaDAO.java            # RF-JDBC-02
        │   ├── ProveedorDAO.java            # RF-JDBC-02
        │   ├── ProductoDAO.java             # RF-JDBC-03 y RF-JDBC-04
        │   ├── ProductoDetalle.java         # DTO del JOIN producto-categoría-proveedor
        │   ├── MovimientoDAO.java           # RF-JDBC-05 y RF-JDBC-06
        │   ├── CopiaInventario.java         # RF-JDBC-07
        │   ├── EstadoValidacion.java
        │   ├── ResultadoValidacionProducto.java
        │   └── DemoTransacciones.java       # RF-JDBC-08
        ├── ui/
        │   └── MenuPrincipal.java           # Menús: pide datos, delega y pinta resultados
        ├── util/                            # LectorConsola y ColoresConsola
        └── complementarias/                 # Ejecutables independientes
            ├── XPathActividad.java          # RF-FIC-07
            ├── AccesoAleatorioActividad.java  # RF-FIC-08
            └── DemoSQLite.java              # Actividad comparativa SQLite
```

## 7. Menús

**Menú principal:**

```text
1. Explorar ficheros y directorios     RF-FIC-01
2. Importar productos desde JSON       RF-FIC-02
3. Importar productos desde XML        RF-FIC-03
4. Consultar productos en memoria      RF-FIC-04
5. Exportar productos                  RF-FIC-05
6. Guardar y recuperar inventario      RF-FIC-06
7. Gestionar base de datos MySQL       UT2
0. Salir
```

**7. Gestión MySQL:**

```text
1. Comprobar conexión y mostrar información de la BD   RF-JDBC-01
2. Gestionar categorías                                RF-JDBC-02
3. Gestionar proveedores                               RF-JDBC-02
4. Gestionar productos (CRUD)                          RF-JDBC-03
5. Consultar y buscar productos                        RF-JDBC-04
6. Registrar entrada o salida de stock                 RF-JDBC-05
7. Consultar historial de movimientos                  RF-JDBC-06
8. Importar productos de memoria a MySQL               RF-JDBC-07
9. Demostración de transacción con commit/rollback     RF-JDBC-08
0. Volver al menú principal
```

- **7.2 Categorías:** alta, listado, modificar nombre, eliminar y consultar por id.
- **7.3 Proveedores:** alta (email opcional), listado, modificar (Intro conserva el dato actual) y eliminar.
- **7.4 Productos:** alta, consultar por código, modificar (nombre, precio, categoría y proveedor; nunca el código ni el stock) y eliminar.
- **7.5 Consultas:** listado con JOIN, búsqueda por código, búsqueda por nombre y stock inferior a un umbral.
- **7.6 Movimientos:** registrar una ENTRADA o una SALIDA.
- **7.7 Historial:** todos los movimientos o los de un producto.

Los mensajes `[OK]` se muestran en verde, `[ERROR]` en rojo y `[AVISO]` en amarillo. Antes de eliminar, modificar o ejecutar una operación se pide confirmación S/N.

## 8. Memoria frente a MySQL

| | Inventario en memoria (UT1) | Base de datos MySQL (UT2) |
|---|---|---|
| Opciones | 1 a 6 del menú principal | Opción 7 y sus submenús |
| Dónde están los datos | Colección Java (`Inventario`) | Tablas de la base `datastock` |
| Persistencia | Se pierden al salir, salvo exportación o serialización | Permanente: siguen tras cerrar y volver a ejecutar |
| Fuente de las consultas | La colección | Siempre MySQL, nunca la colección |

Son **dos almacenes distintos** y no se sincronizan automáticamente:

- Importar un JSON o un XML (opciones 2 y 3) solo carga productos en memoria.
- Los productos pasan a MySQL únicamente con la opción **7.8**, que copia sin modificar ni vaciar el inventario en memoria.
- Los cambios hechos en MySQL no vuelven a la memoria.

## 9. Decisiones de diseño de la UT2

- **Separación consola / acceso a datos:** las clases del paquete `jdbc` no imprimen ni leen de la consola. Devuelven datos, objetos o lanzan `SQLException`; `MenuPrincipal` pide los datos, llama a los métodos y pinta los resultados.
- **Conexiones cortas:** `ConexionBD.abrir()` abre una conexión nueva en cada operación y quien la pide la cierra con try-with-resources (`Connection`, `PreparedStatement`, `ResultSet`). Nunca queda una conexión abierta indefinidamente.
- **`PreparedStatement` siempre:** ningún dato del usuario se concatena en el SQL. Los importes se manejan con `BigDecimal` y `setBigDecimal`. El `ResultSet` va en un segundo try cuando la consulta tiene parámetros.
- **Mensajes de error:** `ErroresSQL.explicar` traduce los códigos de MySQL a español: 1045 (credenciales), 1049 (esquema inexistente), 08xxx (servidor), 1062 (duplicado, distinguiendo clave primaria de campo único), 1451 (baja bloqueada por una clave foránea), 1452 (referencia inexistente) y 1644 (error lanzado por el procedimiento con `SIGNAL`). Nunca muestra la contraseña ni trazas.
- **Validación en Java antes del SQL:** altas y modificaciones reutilizan `ValidadorProducto`. Se comprueba que existan la categoría y el proveedor y que el código no esté repetido. Las restricciones del script siguen siendo la última barrera.
- **RF-JDBC-02:** En `CategoriaDAO` y `ProveedorDAO`, `actualizar` y `eliminar` devuelven si `executeUpdate()` afectó a una fila, para distinguir «no existe» de «hecho». El email vacío se guarda como `NULL` con `setNull`.
- **RF-JDBC-03:** la modificación no permite cambiar el código ni el stock; el stock solo cambia desde los movimientos. La baja de un producto con movimientos la bloquea la clave foránea y no se borra el historial.
- **RF-JDBC-04:** `ProductoDetalle` es un DTO con los nombres de categoría y proveedor que devuelve un JOIN. La búsqueda por nombre usa `LIKE` con `%texto%` como parámetro; no distingue mayúsculas porque la base usa la colación `utf8mb4_unicode_ci`. El umbral usa `stock < ?` (estricto) y admite el 0; se ordena por stock y código.
- **RF-JDBC-05:** `MovimientoDAO.registrar` llama a `{call registrar_movimiento(?, ?, ?)}` con `CallableStatement`. El procedimiento ya contiene su transacción (`START TRANSACTION`, `SELECT ... FOR UPDATE`, `UPDATE`, `INSERT`, `COMMIT` y `ROLLBACK`), por eso Java no abre otra ni toca `producto.stock`. El stock anterior y posterior que se muestra se lee del movimiento registrado, calculado dentro de esa transacción.
- **RF-JDBC-06:** el historial se ordena por `fecha, id`. El menú comprueba primero que el producto existe para distinguir «no existe» de «existe sin movimientos». La fecha se lee con `getObject("fecha", LocalDateTime.class)`: así coincide con la que ve MySQL aunque el PC tenga otra zona horaria que la configurada en la URL.
- **RF-JDBC-07:** `CopiaInventario` separa dos fases. Primero valida cada producto sin transacción (duplicado, datos, categoría y proveedor) y lo clasifica como aceptado, duplicado o rechazado. Después inserta **solo los aceptados** en una única conexión con `setAutoCommit(false)`: `commit()` al terminar y `rollback()` si falla una inserción. Por eso un producto rechazado no provoca rollback. No se crean categorías ni proveedores, y el inventario en memoria no se modifica. Para compartir la conexión, `ProductoDAO.insertar` tiene una sobrecarga que recibe la `Connection` y no la cierra.
- **RF-JDBC-08:** `DemoTransacciones` usa ids reservados (ver más abajo), comprueba antes que están libres y verifica el resultado con consultas desde otra conexión. Restaura `autoCommit` al terminar.
- **Cierre del proceso:** el plugin `exec-maven-plugin` lleva `cleanupDaemonThreads=false` para que `mvn exec:java` no espere al hilo interno del conector MySQL al salir.

### Demostración de transacciones (7.9): ids y limpieza

| Escenario | Qué hace | Ids |
|---|---|---|
| A (commit) | Inserta dos categorías en una transacción y confirma | 901 `DEMO-TX-901`, 902 `DEMO-TX-902` |
| B (rollback) | Inserta una categoría y vuelve a insertar el mismo id: falla con el error 1062 y se hace rollback | 903 `DEMO-TX-903` |

Si alguno de los tres ids ya existe, la demostración **no se ejecuta** y lo avisa. Para borrar únicamente los datos de la demostración:

```sql
DELETE FROM categoria WHERE id IN (901, 902, 903) AND nombre LIKE 'DEMO-TX-%';
```

## 10. Comparativa MySQL y SQLite

| Característica | MySQL | SQLite |
|---|---|---|
| ¿Necesita servidor? | Sí, hay que tener el servicio MySQL arrancado en el PC. | No, es un motor embebido. No hay servicio, es solo un archivo. |
| ¿Dónde se guardan los datos? | En los archivos internos del servidor MySQL. | En un único archivo local (`datos/sqlite/productos_demo.db`). |
| Conexión JDBC | `jdbc:mysql://localhost:3306/datastock` (pide usuario y contraseña). | `jdbc:sqlite:datos/sqlite/productos_demo.db` (solo la ruta del archivo, sin credenciales). |
| Dependencia (`pom.xml`) | `mysql-connector-j` (versión 9.3.0). | `sqlite-jdbc` (versión 3.45.1.0). |
| Ventajas principales | Permite varios usuarios a la vez, gestión de permisos, procedimientos almacenados (como el de stock) y transacciones seguras con bloqueo de filas. | Cero configuración. Copias el archivo `.db` y funciona en cualquier sitio. Es muy rápido para un solo usuario. |
| Inconvenientes | Hay que instalarlo, arrancarlo y administrarlo. Si se cae el servidor, la app no puede acceder a los datos; DataStock lo controla: muestra «no se puede conectar con el servidor MySQL» y sigue funcionando (CP-JDBC-01b). | Si hay muchos usuarios intentando escribir a la vez, da problemas. No tiene usuarios ni procedimientos almacenados. |
| ¿Para qué lo elegiría? | Para la versión real de DataStock, porque varios empleados podrían estar consultando y modificando el stock a la vez. | Para hacer pruebas locales rápidas, prototipos o una app de escritorio que solo va a usar una persona. |

### ¿Qué es un pool de conexiones?

Abrir una conexión con la base de datos es costoso (red, autenticación, sesión en el servidor). Un **pool de conexiones** mantiene un conjunto de conexiones ya abiertas y las presta y las recupera, en lugar de abrir y cerrar una cada vez. Resulta útil cuando hay muchas peticiones simultáneas, como en un servidor web o una aplicación con muchos usuarios.

DataStock es una aplicación de consola de un solo usuario y sus operaciones son puntuales: abre una conexión, la usa y la cierra con try-with-resources. No acumula conexiones ni deja ninguna abierta, así que funciona bien con conexiones cortas y no necesita un pool. En el lote de la opción 7.8 y en la demostración 7.9 se usa una sola conexión durante toda la transacción, porque `commit` y `rollback` afectan a la conexión en la que se ejecutan.

## 11. Pruebas y evidencias

- `docs/DataStock_UT1_Registro_Pruebas.docx` y `.pdf`: 52 pruebas de la UT1.
- `docs/DataStock_UT2_Registro_Pruebas.docx` y `.pdf`: 37 pruebas de la UT2 (CP-JDBC-01 a 20, CP-JDBC-01b, CP-SQLITE-01 a 03 y 13 pruebas adicionales EX-JDBC).
- `evidencias/`: la salida de cada prueba. Cada fichero lleva el identificador de la prueba como nombre (`CP-JDBC-12_entrada.txt`) y los extras empiezan por `extra_`. Las carpetas `jdbc01` a `jdbc08` corresponden a RF-JDBC-01 a 08 y `sqlite` a la actividad comparativa.
- `datos/pruebas/jdbc07/`: ficheros de la importación (`ut2_importacion_mixta.json` y `ut2_fallo_forzado.json`) y el script del trigger temporal con el que se fuerza el fallo del CP-JDBC-18.

## 12. Reglas clave del enunciado

- JSON raíz `productos` y XML raíz `catalogo` con elementos `producto`.
- Los campos del producto son `codigo`, `nombre`, `precio`, `stock`, `categoriaId` y `proveedorId`.
- Los códigos no se duplican, incluso si cambia el uso de mayúsculas/minúsculas.
- Un documento mal formado o con estructura general inválida **no modifica** el inventario.
- Importar JSON/XML **añade** productos; recuperar un `.dat` **sustituye** el inventario, tras confirmar cuando corresponda.
- Las opciones 1 a 6 trabajan solo con la memoria; la opción 7 trabaja solo con MySQL. Solo la 7.8 copia de memoria a MySQL.
- Las restricciones del script SQL no se modifican: las validaciones se hacen en Java y los errores de MySQL se capturan y se explican.
- El stock en MySQL solo cambia mediante el procedimiento `registrar_movimiento`.
- No se suben contraseñas: `db.properties` está en `.gitignore`.
- El fichero `stock.bin` de RF-FIC-08 es una **copia independiente**: los cambios hechos allí no actualizan la colección Java.

## 13. Incidencias conocidas

- **Aviso de SLF4J al ejecutar `DemoSQLite`** («Failed to load class StaticLoggerBinder»): es informativo. El driver busca un logger y usa uno nulo; no afecta al resultado.
- **Consola de Windows:** para ver correctamente el símbolo € hay que ejecutar `chcp 65001` y después `mvn exec:java` en la misma terminal. El botón Run del IDE abre una terminal nueva que no conserva ese ajuste.
- **Hora de los movimientos:** la fecha de `movimiento_stock` la pone MySQL con la zona horaria de su servidor, por lo que puede variar entre dos equipos con servidores configurados de forma distinta.

## Decisiones de diseño de la UT1

- **Separación menú / lógica:** `MenuPrincipal` solo pide datos, delega y muestra
  resultados. Las operaciones con ficheros están en el paquete `ficheros`, cuyas
  clases no dependen de la consola.
- **RF-FIC-01:** `ExploradorRutas` usa la API NIO (`Path`, `Files`).
  `Files.list` se abre con try-with-resources. Se controlan por separado
  las rutas vacías, inexistentes, con caracteres no válidos y sin permisos.
- **Validación común:** `ValidadorProducto` recibe los seis campos como texto y
  aplica las reglas de la sección 6 del enunciado. La usan los importadores JSON
  y XML y la recuperación del `.dat`, por lo que todos los caminos de entrada
  validan exactamente igual. `validar()` acumula todos los errores de un
  producto; `crear()` solo se llama cuando no hay errores.
- **Normalización de códigos:** se eliminan los espacios extremos y se pasan
  a mayúsculas antes de validar y de comparar duplicados. Así `tec-001` se
  acepta como `TEC-001`, coherente con que la unicidad no distinga mayúsculas.
- **Precio con dos decimales como máximo:** se valida ya en UT1, coherente con
  el tipo `DECIMAL(10,2)` que tendrá en MySQL.
- **Resultado de importación:** los importadores no imprimen nada; devuelven un
  `ResultadoImportacion` con encontrados, importados, rechazados e incidencias,
  o un error general si el documento no puede procesarse. El menú se encarga
  de mostrarlo, con `[OK]` en verde y `[ERROR]` en rojo, mediante un único
  método común para JSON y XML.
- **RF-FIC-02:** `ImportadorJson` lee el documento completo con Gson antes de
  tocar el inventario. Si el JSON está mal formado o no tiene el array
  `productos`, se rechaza entero. Los productos válidos se guardan primero en
  una lista temporal y solo al final se añaden al inventario. Los códigos
  repetidos dentro del mismo fichero se detectan con un `Set`; los que ya
  existen en el inventario, con `Inventario.agregar()`.
- **RF-FIC-03:** `ImportadorXml` analiza el documento con DOM. La fábrica
  prohíbe el `DOCTYPE` (`disallow-doctype-decl`), lo que impide DTD y entidades
  externas y protege frente a ataques XXE. El analizador usa un `DefaultHandler`
  para no escribir errores por consola. La lectura de cada producto, la
  validación y el alta en el inventario siguen el mismo esquema que en JSON.
  Las comprobaciones de ruta y el procesamiento de productos están repetidos en
  los dos importadores; extraerlos a un componente común queda previsto para UT3.
- **RF-FIC-04:** las búsquedas y el listado están en `Inventario`, que devuelve
  resultados sin imprimir. `listarOrdenado()` ordena una **copia** con
  `Comparator.comparing(Producto::getCodigo)`, de modo que consultar no altera
  la colección. El menú muestra los resultados con un único método auxiliar.
- **RF-FIC-05:** `ExportadorJson` (Gson) y `ExportadorXml` (DOM + `Transformer`)
  reciben la lista ordenada y el destino, escriben en UTF-8 con indentación y
  no imprimen nada; los errores se lanzan como `IOException` para que el menú
  los muestre. El XML convierte las excepciones propias de DOM y del
  `Transformer` en `IOException`, de modo que el menú trata igual ambos
  formatos. Las preguntas al usuario (ruta, carpeta existente, confirmación
  de sobrescritura) están en un único método del menú, `pedirRutaDestino()`,
  que también usa el guardado del `.dat`. No hay conversor directo JSON↔XML:
  el modelo intermedio es la colección Java. Los métodos de exportación del
  menú son casi iguales; unificarlos mediante una interfaz común queda previsto
  para UT3.
- **RF-FIC-06:** `SerializadorInventario` guarda una copia de la lista con
  `ObjectOutputStream` y la recupera con `ObjectInputStream`. Al recuperar se
  comprueba que el objeto leído es una lista, que cada elemento es un `Producto`
  y que cumple las reglas de `ValidadorProducto`. El menú lee y valida primero,
  después pide confirmación si hay productos en memoria, y solo entonces llama a
  `Inventario.sustituir()`: si el fichero está dañado o el usuario cancela, el
  inventario no cambia. Importar JSON/XML **añade** productos; recuperar un
  `.dat` **sustituye** el inventario. La serialización Java es un formato binario
  propio de Java, no un formato de intercambio: solo deben abrirse ficheros
  generados por DataStock.
- **RF-FIC-07:** `XPathActividad` es una clase ejecutable independiente que
  consulta el XML directamente, sin importarlo al inventario. Reutiliza la
  configuración anti-XXE de RF-FIC-03. Para no inyectar texto del usuario en
  la expresión, la búsqueda por código obtiene todos los productos con
  `/catalogo/producto` y compara el código en Java; la categoría y el precio se
  convierten antes a número (`Integer`, `BigDecimal`), por lo que solo pueden
  contener valores numéricos válidos. El filtro de precio usa `>` estricto.
  El contador incluye todos los elementos `producto`, aunque tengan datos
  incorrectos, porque XPath consulta el fichero tal cual, sin validarlo.
- **RF-FIC-08:** `AccesoAleatorioActividad` es una clase ejecutable
  independiente. Como no comparte la memoria del menú, carga los productos desde
  un inventario `.dat` guardado con RF-FIC-06, que es la copia en disco de los
  productos que estaban en memoria. Genera `stock.bin` con registros de 36 bytes
  (código de 12 caracteres = 24 bytes, precio `double` = 8, stock `int` = 4),
  ordenados por código y truncando el fichero antes de escribir. La consulta
  usa `seek(posición × 36)` y la modificación `seek(posición × 36 + 32)`,
  reposicionando tras leer el valor anterior. Se comprueba que el tamaño sea
  múltiplo de 36. `stock.bin` es una copia independiente: modificarlo no
  cambia el `.dat` ni el inventario.
- **Ejecución de las actividades:** el `pom.xml` define la propiedad
  `exec.mainClass` (por defecto `App`), lo que permite lanzar las actividades
  complementarias con `-Dexec.mainClass=...`. En el proyecto base la clase
  principal estaba fijada en el plugin y ese parámetro se ignoraba.
- **Consola de Windows:** para ver correctamente el símbolo € hay que ejecutar
  `chcp 65001` y después `mvn exec:java` en la misma terminal. El botón Run
  del IDE abre una terminal nueva que no conserva ese ajuste.

## Referencia

El **enunciado DataStock** facilitado en el aula virtual (UT1 y UT2) es la fuente que determina el comportamiento exigido. El script SQL y el diagrama entidad-relación están en `material_referencia/`.