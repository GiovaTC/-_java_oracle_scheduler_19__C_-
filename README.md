# -_java_oracle_scheduler_19__C_- :.
Java Oracle Scheduler 19c — Proyecto completo:

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/99613687-6a26-4f5e-b518-6ece6761961f" />  

<img width="2558" height="1078" alt="image" src="https://github.com/user-attachments/assets/e53d964a-89f2-47e9-b773-0cff7195c48a" />     

```

Aplicación de consola desarrollada con Java 21, IntelliJ IDEA, Maven, JDBC y Oracle Database 19c. Permite consultar,
ejecutar, habilitar y deshabilitar un trabajo (JOB) administrado por DBMS_SCHEDULER, además de
consultar las solicitudes y el historial de procesamiento.

1. Funcionalidad
- Conectarse a Oracle 19c mediante JDBC.
- Consultar el estado del JOB JOB_PROCESAR_SOLICITUDES.
- Ejecutar manualmente el JOB desde Java.
- Habilitar la programación automática cada cinco minutos.
- Deshabilitar la programación automática.
- Consultar solicitudes pendientes y procesadas.
- Consultar la bitácora de ejecuciones.

Importante: el JOB se crea inicialmente deshabilitado. La ejecución automática es administrada por Oracle y no requiere que la aplicación Java permanezca abierta.

2. Requisitos
- Oracle Database 19c.
- JDK 21.
- IntelliJ IDEA.
- Maven 3.8 o superior.
- SQL Developer o SQL*Plus para ejecutar los scripts.

3. Estructura del proyecto
JavaOracleScheduler19c/
├── pom.xml
├── README.md
├── sql/
│   ├── 01_crear_base_datos.sql
│   └── 02_consultas_verificacion.sql
└── src/
    └── main/
        └── java/
            ├── Main.java
            ├── config/
            │   └── ConexionOracle.java
            └── servicio/
                └── JobServicio.java

4. Código completo
4.1 pom.xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.giovatc</groupId>
    <artifactId>java-oracle-scheduler-19c</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc11</artifactId>
            <version>23.6.0.24.10</version>
        </dependency>
    </dependencies>
</project>

4.2 src/main/java/config/ConexionOracle.java
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionOracle {

    private static final String URL = System.getenv().getOrDefault(
            "ORACLE_URL",
            "jdbc:oracle:thin:@//localhost:1521/orcl"
    );

    private static final String USUARIO = System.getenv().getOrDefault(
            "ORACLE_USER",
            "USUARIO_APP"
    );

    private static final String CLAVE = System.getenv().getOrDefault(
            "ORACLE_PASSWORD",
            "CAMBIAR_CLAVE"
    );

    private ConexionOracle() {
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}

Cambie USUARIO_APP, CAMBIAR_CLAVE y orcl por el usuario, contraseña y servicio reales de su instalación. También puede definir las variables de entorno descritas más adelante.

4.3 src/main/java/servicio/JobServicio.java
package servicio;

import config.ConexionOracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JobServicio {

    private static final String JOB = "JOB_PROCESAR_SOLICITUDES";

    // Ejecuta el JOB en la sesión actual.
    public void ejecutarJob() throws SQLException {

        String sql = """
                BEGIN
                    DBMS_SCHEDULER.RUN_JOB(
                        job_name => ?,
                        use_current_session => TRUE
                    );
                END;
                """;

        try (Connection cn = ConexionOracle.conectar();
             CallableStatement st = cn.prepareCall(sql)) {

            st.setString(1, JOB);
            st.execute();

            System.out.println("JOB ejecutado correctamente.");
        }
    }

    // Habilita la programación automática del JOB.
    public void habilitarJob() throws SQLException {

        ejecutarAccionScheduler(
                "BEGIN DBMS_SCHEDULER.ENABLE(name => ?); END;"
        );

        System.out.println("JOB habilitado correctamente.");
    }

    // Deshabilita la programación automática del JOB.
    public void deshabilitarJob() throws SQLException {

        ejecutarAccionScheduler(
                "BEGIN DBMS_SCHEDULER.DISABLE(name => ?, force => TRUE); END;"
        );

        System.out.println("JOB deshabilitado correctamente.");
    }

    private void ejecutarAccionScheduler(String sql) throws SQLException {

        try (Connection cn = ConexionOracle.conectar();
             CallableStatement st = cn.prepareCall(sql)) {

            st.setString(1, JOB);
            st.execute();
        }
    }

    // Consulta el estado del JOB.
    public void listarJobs() throws SQLException {

        String sql = """
                SELECT job_name, enabled, state, repeat_interval
                FROM user_scheduler_jobs
                WHERE job_name = ?
                """;

        try (Connection cn = ConexionOracle.conectar();
             PreparedStatement st = cn.prepareStatement(sql)) {

            st.setString(1, JOB);

            try (ResultSet rs = st.executeQuery()) {

                System.out.println("\n--- ESTADO DEL JOB ---");

                if (!rs.next()) {
                    System.out.println(
                            "No se encontró el JOB. Ejecute primero el script SQL."
                    );
                    return;
                }

                do {
                    System.out.printf(
                            "Nombre: %s | Habilitado: %s | Estado: %s | Frecuencia: %s%n",
                            rs.getString("job_name"),
                            rs.getString("enabled"),
                            rs.getString("state"),
                            rs.getString("repeat_interval")
                    );
                } while (rs.next());
            }
        }
    }

    // Consulta los últimos 20 registros de la bitácora.
    public void consultarHistorial() throws SQLException {

        String sql = """
                SELECT log_id, fecha_ejecucion, registros_procesados, mensaje
                FROM job_log
                ORDER BY log_id DESC
                FETCH FIRST 20 ROWS ONLY
                """;

        try (Connection cn = ConexionOracle.conectar();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            System.out.println("\n--- HISTORIAL DE PROCESAMIENTO ---");

            boolean hayDatos = false;

            while (rs.next()) {
                hayDatos = true;

                System.out.printf(
                        "ID: %d | Fecha: %s | Procesados: %d | Mensaje: %s%n",
                        rs.getLong("log_id"),
                        rs.getTimestamp("fecha_ejecucion"),
                        rs.getInt("registros_procesados"),
                        rs.getString("mensaje")
                );
            }

            if (!hayDatos) {
                System.out.println("Todavía no hay ejecuciones registradas.");
            }
        }
    }

    // Consulta las solicitudes registradas.
    public void consultarSolicitudes() throws SQLException {

        String sql = """
                SELECT id, descripcion, estado, fecha_proceso
                FROM job_solicitudes
                ORDER BY id
                """;

        try (Connection cn = ConexionOracle.conectar();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            System.out.println("\n--- SOLICITUDES ---");

            while (rs.next()) {
                System.out.printf(
                        "%d | %-35s | %-12s | %s%n",
                        rs.getLong("id"),
                        rs.getString("descripcion"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_proceso")
                );
            }
        }
    }
}

4.4 src/main/java/Main.java
import config.ConexionOracle;
import servicio.JobServicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        JobServicio servicio = new JobServicio();

        // Verifica la conexión antes de mostrar el menú.
        try (Connection cn = ConexionOracle.conectar();
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Conexión exitosa con Oracle Database 19c.");

            boolean continuar = true;

            while (continuar) {

                System.out.println("\n======================================");
                System.out.println("   ADMINISTRADOR DE JOB - ORACLE 19c");
                System.out.println("======================================");
                System.out.println("1. Consultar estado del JOB");
                System.out.println("2. Ejecutar JOB ahora");
                System.out.println("3. Habilitar ejecución programada");
                System.out.println("4. Deshabilitar ejecución programada");
                System.out.println("5. Consultar historial");
                System.out.println("6. Consultar solicitudes");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");

                String opcion = sc.nextLine().trim();

                try {
                    switch (opcion) {
                        case "1" -> servicio.listarJobs();
                        case "2" -> servicio.ejecutarJob();
                        case "3" -> servicio.habilitarJob();
                        case "4" -> servicio.deshabilitarJob();
                        case "5" -> servicio.consultarHistorial();
                        case "6" -> servicio.consultarSolicitudes();
                        case "0" -> continuar = false;
                        default -> System.out.println("Opción no válida.");
                    }

                } catch (SQLException e) {
                    System.err.println("Error Oracle: " + e.getMessage());
                    System.err.println("Código Oracle: " + e.getErrorCode());
                }
            }

            System.out.println("Aplicación finalizada.");

        } catch (SQLException e) {
            System.err.println(
                    "No fue posible conectar con Oracle: " + e.getMessage()
            );
            System.err.println(
                    "Verifique URL, usuario, contraseña y servicio de Oracle."
            );
        }
    }
}

5. Scripts de Oracle Database 19c
Ejecute el siguiente script conectado con el usuario propietario del proyecto.
El usuario debe tener permisos para crear tablas, procedimientos y JOBs mediante DBMS_SCHEDULER .

5.1 sql/01_crear_base_datos.sql
-- =====================================================
-- PROYECTO: JAVA ORACLE SCHEDULER 19C
-- OBJETIVO: CREAR TABLAS, PROCEDIMIENTO Y JOB
-- =====================================================

-- TABLA DE SOLICITUDES
CREATE TABLE job_solicitudes (
    id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    descripcion VARCHAR2(150) NOT NULL,
    estado VARCHAR2(20) DEFAULT 'PENDIENTE' NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    fecha_proceso TIMESTAMP,

    CONSTRAINT chk_job_solicitudes_estado
        CHECK (estado IN ('PENDIENTE', 'PROCESADO'))
);

-- TABLA DE HISTORIAL
CREATE TABLE job_log (
    log_id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    fecha_ejecucion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    registros_procesados NUMBER DEFAULT 0 NOT NULL,
    mensaje VARCHAR2(1000)
);

-- PROCEDIMIENTO ALMACENADO
CREATE OR REPLACE PROCEDURE pr_procesar_solicitudes AS

    v_total NUMBER := 0;

BEGIN

    UPDATE job_solicitudes
       SET estado = 'PROCESADO',
           fecha_proceso = SYSTIMESTAMP
     WHERE estado = 'PENDIENTE';

    v_total := SQL%ROWCOUNT;

    INSERT INTO job_log (
        fecha_ejecucion,
        registros_procesados,
        mensaje
    )
    VALUES (
        SYSTIMESTAMP,
        v_total,
        'Procesamiento finalizado correctamente'
    );

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN

        ROLLBACK;

        INSERT INTO job_log (
            fecha_ejecucion,
            registros_procesados,
            mensaje
        )
        VALUES (
            SYSTIMESTAMP,
            0,
            SUBSTR('ERROR: ' || SQLERRM, 1, 1000)
        );

        COMMIT;

        RAISE;

END;
/

-- CREACIÓN DEL JOB: QUEDA DESHABILITADO INICIALMENTE.
BEGIN

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_PROCESAR_SOLICITUDES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'PR_PROCESAR_SOLICITUDES',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=5',
        enabled         => FALSE,
        auto_drop       => FALSE,
        comments        => 'Procesa solicitudes pendientes cada cinco minutos'
    );

END;
/

-- DATOS DE PRUEBA
INSERT INTO job_solicitudes (descripcion, estado)
VALUES ('Solicitud de apertura de cuenta', 'PENDIENTE');

INSERT INTO job_solicitudes (descripcion, estado)
VALUES ('Actualización de datos de cliente', 'PENDIENTE');

INSERT INTO job_solicitudes (descripcion, estado)
VALUES ('Generación de reporte mensual', 'PENDIENTE');

INSERT INTO job_solicitudes (descripcion, estado)
VALUES ('Validación de producto', 'PENDIENTE');

INSERT INTO job_solicitudes (descripcion, estado)
VALUES ('Solicitud procesada de ejemplo', 'PROCESADO');

COMMIT;

-- VALIDACIÓN INICIAL
SELECT * FROM job_solicitudes ORDER BY id;

SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_PROCESAR_SOLICITUDES';

5.2 sql/02_consultas_verificacion.sql
-- CONSULTAR ESTADO DEL JOB
SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_PROCESAR_SOLICITUDES';

-- CONSULTAR SOLICITUDES
SELECT *
FROM job_solicitudes
ORDER BY id;

-- CONTAR REGISTROS POR ESTADO
SELECT estado, COUNT(*) AS cantidad
FROM job_solicitudes
GROUP BY estado;

-- CONSULTAR BITÁCORA DE LA APLICACIÓN
SELECT *
FROM job_log
ORDER BY log_id DESC;

-- CONSULTAR HISTORIAL ADMINISTRADO POR ORACLE
SELECT
    log_date,
    status,
    actual_start_date,
    run_duration,
    additional_info
FROM user_scheduler_job_run_details
WHERE job_name = 'JOB_PROCESAR_SOLICITUDES'
ORDER BY log_date DESC;

6. Configuración de la conexión Oracle
La conexión predeterminada del ejemplo es:
URL: jdbc:oracle:thin:@//localhost:1521/orcl
USUARIO: USUARIO_APP
CONTRASEÑA: CAMBIAR_CLAVE
Puede modificar los valores en ConexionOracle.java.

También puede configurar variables de entorno en Windows PowerShell:
$env:ORACLE_URL="jdbc:oracle:thin:@//localhost:1521/orcl"
$env:ORACLE_USER="USUARIO_APP"
$env:ORACLE_PASSWORD="SU_CLAVE"
Ajuste orcl al nombre real del servicio de su base de datos.

7. Ejecución desde IntelliJ IDEA
  1. Abra IntelliJ IDEA.
  2. Seleccione File > Open.
  3. Abra la carpeta JavaOracleScheduler19c.
  4. Configure el SDK del proyecto en Java 21.
  5. Espere a que Maven descargue la dependencia ojdbc11.
  6. Abra Main.java.
  7. Ejecute el método main().

Antes de iniciar la aplicación, ejecute 01_crear_base_datos.sql desde SQL Developer o SQL*Plus.

8. Prueba paso a paso
Paso 1. Consultar solicitudes
Seleccione la opción 6.

Inicialmente deben aparecer cuatro solicitudes pendientes y una procesada.
Paso 2. Ejecutar el JOB manualmente
Seleccione la opción 2.

La aplicación invoca:
BEGIN
    DBMS_SCHEDULER.RUN_JOB(
        job_name => 'JOB_PROCESAR_SOLICITUDES',
        use_current_session => TRUE
    );
END;

Paso 3. Verificar el procesamiento
Seleccione la opción 6.

Las cuatro solicitudes pendientes deben cambiar a PROCESADO.

Paso 4. Consultar el historial
Seleccione la opción 5.

Debe aparecer una entrada en JOB_LOG con la cantidad de registros procesados.

Paso 5. Activar la programación automática
Seleccione la opción 3.

Oracle ejecutará el JOB de acuerdo con su frecuencia configurada: cada cinco minutos.
Paso 6. Desactivar la programación automática
Seleccione la opción 4.

El JOB dejará de ejecutarse de acuerdo con su calendario.

9. Resultado esperado en consola
Conexión exitosa con Oracle Database 19c.

======================================
   ADMINISTRADOR DE JOB - ORACLE 19c
======================================
1. Consultar estado del JOB
2. Ejecutar JOB ahora
3. Habilitar ejecución programada
4. Deshabilitar ejecución programada
5. Consultar historial
6. Consultar solicitudes
0. Salir

Seleccione una opción: 2

JOB ejecutado correctamente.
Ejemplo de historial esperado:
--- HISTORIAL DE PROCESAMIENTO ---
ID: 1 | Procesados: 4 | Mensaje: Procesamiento finalizado correctamente

10. Privilegios y consideraciones de Oracle
- Ejecute los scripts con el usuario propietario del esquema.
- El usuario debe disponer de los privilegios necesarios para crear tablas, procedimientos y trabajos programados.
- Si el DBA administra los permisos, puede evaluar conceder CREATE JOB al usuario de aplicación.
- DBMS_SCHEDULER debe estar disponible y habilitado en la instancia.
- RUN_JOB con use_current_session => TRUE ejecuta el trabajo en la sesión que lo solicita y permite recibir errores en Java.
- La ejecución automática es administrada por Oracle, por lo que Java puede cerrarse después de habilitar el JOB.
- Para procesos de alto volumen, se recomienda implementar procesamiento por lotes, control de concurrencia, reintentos y monitoreo.
Nota sobre transacciones: el procedimiento de demostración confirma (COMMIT) el procesamiento y registra errores antes de volver a lanzarlos. Para procesos productivos, conviene diseñar la estrategia de transacciones y auditoría de acuerdo con las reglas del negocio.

11. Errores frecuentes
Error	Posible solución
ORA-01017	Verificar usuario y contraseña.
ORA-12514	Validar el nombre del servicio Oracle.
ORA-12541	Revisar que el listener esté activo.
ORA-27486	Solicitar los privilegios necesarios para Scheduler.
ORA-27475	Verificar el nombre del JOB y el esquema.
ORA-06550	Revisar errores de compilación del procedimiento.

12. Tecnologías utilizadas
- Java 21.
- IntelliJ IDEA.
- Maven.
- JDBC.
- Oracle JDBC ojdbc11.
- Oracle Database 19c.
- PL/SQL.
- DBMS_SCHEDULER.

Proyecto: Java Oracle Scheduler 19c
Tipo: Aplicación de consola
Objetivo: Ejecutar y administrar trabajos programados en Oracle Database 19c desde Java .
:. . / .  
