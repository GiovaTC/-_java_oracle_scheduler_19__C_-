package servicio;

import config.ConexionOracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JobServicio {

    private  static final String JOB = "JOB_PROCESAR_SOLICITUDES";

    // EJECUTA el JOB en la sesion actual!
    public void ejecutarJob() throws SQLException
    {
        String sql = """
                BEGIN
                    DBMS_SCHEDULER.RUN_JOB(
                        job_name => ?,
                        use_current_session => TRUE);
                END;
                """;

        try (Connection cn = ConexionOracle.conectar();
             CallableStatement st = cn.prepareCall(sql))
        {
            st.setString(1, JOB);
            st.execute();

            System.out.println("JOB EJECUTADO CORRECTAMENTE!");
        }
    }

    // HABILITA la programacion automatica del JOB!
    public void habilitarJob() throws SQLException {

        ejecutarAccionScheduler("BEGIN DBMS_SCHEDULER.ENABLE(name => ?); END;");

        System.out.println("JOB HABILITADO correctamente!");
    }

    // DESHABILITA la programacion automatica del JOB!
    public void deshabilitarJob() throws SQLException
    {

        ejecutarAccionScheduler("BEGIN DBMS_SCHEDULER.DISABLE(name => ?, force => TRUE); END;");

        System.out.println("JOB deshabilitado correctamente!");
    }

    private void ejecutarAccionScheduler(String sql) throws SQLException
    {

        try (Connection cn = ConexionOracle.conectar();
             CallableStatement st = cn.prepareCall(sql))
        {
            st.setString(1, JOB);
            st.execute();
        }
    }

    // CONSULTA EL ESTADO DEL JOB!
    public void listarJobs() throws SQLException
    {

        String sql = """
                SELECT job_name, enabled, state, repeat_interval
                FROM user_scheduler_jobs
                WHERE job_name = ?
                """;

        try  (Connection cn = ConexionOracle.conectar();
              PreparedStatement st = cn.prepareStatement(sql))
        {
            st.setString(1, JOB);

            try (ResultSet rs = st.executeQuery())
            {
                System.out.println("\n--- ESTADO DEL JOB ---");

                if (!rs.next())
                {
                    System.out.println("NO se encontro el JOB. EJECUTE PRIMERO el script SQL!");

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
}
