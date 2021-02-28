package chat;

import chat.auth.AuthService;
import chat.auth.BaseAuthService;
import chat.handler.ClientHandler;
import clientserver.Command;
import org.apache.log4j.PropertyConfigurator;


import java.util.logging.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;



public class MyServer {


    private final ServerSocket serverSocket;
    private final AuthService authService;
    private final List<ClientHandler> clients = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(MyServer.class.getName());


    public MyServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.authService = new BaseAuthService();
        logger.setLevel(Level.ALL);
        Handler handler = new FileHandler("C:\\Users\\Виталий\\Desktop\\java_ult\\ChatServer\\src\\main\\resources\\logs\\log4j.log");
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        PropertyConfigurator.configure("C:\\Users\\Виталий\\Desktop\\java_ult\\ChatServer\\src\\main\\resources\\logs\\configs\\log4j.properties");

    }

    public void start() throws IOException {
        logger.log(Level.INFO, "Сервер запущен!");
//        System.out.println("Сервер запущен!");

        try {
            while (true) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка создания нового подключения", e.getMessage());
//            System.out.println("Ошибка создания нового подключения");
//            e.printStackTrace();
        } finally {
            serverSocket.close();
            logger.log(Level.INFO, "Сервер закрыт");
        }
    }

    private void waitAndProcessNewClientConnection() throws IOException {
        logger.log(Level.INFO, "Сервер ожидает подключения пользователя к порту " + serverSocket.getLocalPort() + "...");
//        System.out.println("Ожидание пользователя...");
        Socket clientSocket = serverSocket.accept();

        logger.log(Level.INFO, "Клиент подключился!");
//        System.out.println("Клиент подключился!");
        processClientConnection(clientSocket);
    }

    private void processClientConnection(Socket clientSocket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isUsernameBusy(String clientUsername) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(clientUsername)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        List<String> usernames = getAllUsernames();
        broadcastMessage(null, Command.updateUsersListCommand(usernames));
        logger.log(Level.FINE, "User " + clientHandler.getUsername() + " подключен");
    }

    public List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler client : clients) {
            usernames.add(client.getUsername());
        }
        return usernames;
    }

    public synchronized void unSubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        List<String> usernames = getAllUsernames();
        broadcastMessage(null, Command.updateUsersListCommand(usernames));
        logger.log(Level.FINE, "User " + clientHandler.getUsername() + " откключен");
    }

    public synchronized void broadcastMessage(ClientHandler sender, Command command) {
        logger.log(Level.FINEST,"User " + sender + " отправлено сообщение");
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendMessage(command);

        }
    }

    public synchronized void sendPrivateMessage(String recipient, Command command) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendMessage(command);
                logger.log(Level.FINEST,"User " + client.getUsername() + " написал записку");
                break;
            }
        }
    }

    public List<ClientHandler> getClients() {
        return clients;
    }


}
