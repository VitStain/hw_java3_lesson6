package chat.auth;

import org.apache.log4j.PropertyConfigurator;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseAuthService implements AuthService {

    private static Connection connection;
    private static Statement stmt;
    private static ResultSet rs;
    private static final Logger logger = Logger.getLogger(BaseAuthService.class.getName());

    private  static void connection() throws ClassNotFoundException, SQLException {
        logger.setLevel(Level.ALL);
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Виталий\\Desktop\\java_ult\\ChatServer\\src\\main\\resources\\db\\main.db");
        logger.log(Level.INFO, "Подключен к базе данных");
        stmt = connection.createStatement();
    }

    private  static void disconnection() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            logger.log(Level.SEVERE, throwables.getMessage(), throwables);
        }
    }


    @Override
    public void start() {
//        System.out.println("Сервис аутентификации запущен");
        logger.log(Level.INFO, "Сервис аутентификации запущен");
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) throws SQLException, ClassNotFoundException {
        connection();
        rs = stmt.executeQuery(String.format("SELECT password, username FROM users WHERE login = '%s'", login));
        String username = rs.getString("username");
        System.out.println(rs.getString("username"));

        if(rs.getString("password").equals(password)) {
            disconnection();
            return username;
            }
        disconnection();
        return null;
    }

    @Override
    public void close() {
//        System.out.println("Сервис аутентификации завершен");
        logger.log(Level.INFO, "Сервис аутентификации завершен");
    }
}
