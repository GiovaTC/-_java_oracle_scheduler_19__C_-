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

    private void ejecutarAccionScheduler(String s) {
    }   
}
