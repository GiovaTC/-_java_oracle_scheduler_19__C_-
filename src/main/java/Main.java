import config.ConexionOracle;
import servicio.JobServicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main
{

    public static void main(String[] args) {
        JobServicio servicio = new JobServicio();

        // VERIFICA la conexion antes de mostrar el menu!
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
