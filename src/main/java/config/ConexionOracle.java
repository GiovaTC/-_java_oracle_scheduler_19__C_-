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
            "system"
    );

    private static final String CLAVE = System.getenv().getOrDefault(
            "ORACLE_PASSWORD",
            "Tapiero123"
    );

    private ConexionOracle()
    {

    }

    


}
