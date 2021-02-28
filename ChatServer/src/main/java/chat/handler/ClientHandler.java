package chat.handler;

import chat.MyServer;
import chat.auth.AuthService;
import clientserver.Command;
import clientserver.CommandType;
import clientserver.commands.*;
import org.apache.log4j.PropertyConfigurator;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.util.logging.*;


public class ClientHandler {

    private final MyServer myServer;
    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private static Connection connection;
    private static Statement stmt;
    private static ResultSet rs;

    private static void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Виталий\\Desktop\\java_ult\\ChatServer\\src\\main\\resources\\db\\main.db");
        stmt = connection.createStatement();
        logger.log(Level.INFO, "Подключен к базе данных");

    }

    private static void disconnection() throws SQLException {
        try {
            connection.close();
        } catch (SQLException throwables) {
//            throwables.printStackTrace();
            logger.log(Level.SEVERE, throwables.getMessage(), throwables);
        }


    }

    public ClientHandler(MyServer myServer, Socket clientSocket) throws IOException {
        this.myServer = myServer;
        this.clientSocket = clientSocket;

        logger.setLevel(Level.ALL);

            Handler handler = new FileHandler("C:\\Users\\Виталий\\Desktop\\java_ult\\ChatServer\\src\\main\\resources\\logs\\log4j.log");

        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        PropertyConfigurator.configure("C:\\Users\\Виталий\\Desktop\\java_ult\\ChatServer\\src\\main\\resources\\logs\\configs\\log4j.properties");
    }

    public void handle() throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
//                System.out.println(e.getMessage());
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }).start();

    }

    private void authentication() throws IOException {

        while (true) {

            Command command = readCommand();
            if (command == null) {
                continue;
            }
            if (command.getType() == CommandType.AUTH) {

                boolean isSuccessAuth = processAuthCommand(command);

                if (isSuccessAuth) {
                    break;
                }

            } else {
                sendMessage(Command.authErrorCommand("Ошибка действия"));
                logger.log(Level.FINE, "Ошибка аутентификации");
            }
        }

    }

    private boolean processAuthCommand(Command command) {
        AuthCommandData cmdData = (AuthCommandData) command.getData();
        String login = cmdData.getLogin();
        String password = cmdData.getPassword();

        AuthService authService = myServer.getAuthService();
        try {
            this.username = authService.getUsernameByLoginAndPassword(login, password);
        } catch (SQLException throwables) {
//            throwables.printStackTrace();
            logger.log(Level.WARNING, throwables.getMessage(), throwables);
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
            logger.log(Level.WARNING, e.getMessage(), e);

        }
        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                sendMessage(Command.authErrorCommand("Логин уже используется"));
                return false;
            }

            UpdateUsersListCommandData.users.clear();
            for (ClientHandler client : myServer.getClients()) {
                UpdateUsersListCommandData.users.add(client.getUsername());
            }


            sendMessage(Command.authOkCommand(username));
            String message = String.format(">>> %s присоединился к чату", username);
            logger.log(Level.FINER,"User " + this.username + " присоединился к чату");
            myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
            myServer.subscribe(this);
            return true;
        } else {
            sendMessage(Command.authErrorCommand("Логин или пароль не соответствуют действительности"));
            logger.log(Level.FINE, "Пользователь не проверен");
            return false;
        }
    }


    private Command readCommand() throws IOException {
        try {
            return (Command) in.readObject();
        } catch (ClassNotFoundException e) {
            String errorMessage = "Получен неизвестный объект";
            System.err.println(errorMessage);
//            e.printStackTrace();
            logger.log(Level.WARNING, e.getMessage(), errorMessage);
            return null;
        }
    }

    private void readMessage() throws IOException {

        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END:
                    String messageExit = String.format(">>> %s покинул чат", username);
                    logger.log(Level.FINER,"User " + this.username + " покинул чат");
                    myServer.broadcastMessage(this, Command.messageInfoCommand(messageExit, null));
                    myServer.unSubscribe(this);
                    return;
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    String message = data.getMessage();
                    String sender = data.getSender();
                    myServer.broadcastMessage(this, Command.messageInfoCommand(message, sender));
                    break;
                }
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String message = data.getMessage();
                    myServer.sendPrivateMessage(recipient, Command.messageInfoCommand(message, username));
                    break;
                }
                case CHANGE_NAME: {
                    logger.log(Level.FINER,"User " + this.username + " пытается изменить имя");
                    ChangeNameCommandData data = (ChangeNameCommandData) command.getData();
                    String lastUsername = data.getLastUsername();
                    String username = data.getUsername();

                    try {
                        connection();
                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
                        logger.log(Level.WARNING, e.getMessage(), e);
                    } catch (SQLException throwables) {
//                        throwables.printStackTrace();
                        logger.log(Level.WARNING, throwables.getMessage(), throwables);
                    }
                    try {
                        int result = stmt.executeUpdate(String.format("UPDATE users SET username = '%s' WHERE username = '%s';", username, lastUsername));
                        try {
                            disconnection();
                        } catch (SQLException throwables) {
//                            throwables.printStackTrace();
                            logger.log(Level.WARNING, throwables.getMessage(), throwables);
                        }
                        if (result == 1) {


                            myServer.sendPrivateMessage(lastUsername, Command.changeNameOkCommand(username));
                            this.username = username;
                            UpdateUsersListCommandData.users.clear();
                            for (ClientHandler client : myServer.getClients()) {
                                UpdateUsersListCommandData.users.add(client.getUsername());
                            }

                            myServer.broadcastMessage(null, Command.updateUsersListCommand(myServer.getAllUsernames()));
                            logger.log(Level.FINER,"User " + this.username +
                                    " изменено имя на " + lastUsername);
                            String messageChangeName = String.format(">>> %s сменил имя на %s", lastUsername, username);
                            myServer.broadcastMessage(this, Command.messageInfoCommand(messageChangeName, null));


                        } else {
                            sendMessage(Command.changeNameErrorCommand("Логин уже используется"));
                            logger.log(Level.FINER,"User " + this.username +
                                    " Логин уже используется ");
                        }

                    } catch (SQLException throwables) {
//                        throwables.printStackTrace();
                        logger.log(Level.WARNING, throwables.getMessage(), throwables);
                    }

                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(Command command) {
        try {
            out.writeObject(command);
        } catch (IOException e) {
//            e.printStackTrace();
            logger.log(Level.WARNING, e.getMessage(), e);
        }
//        out.flush();
    }
}
