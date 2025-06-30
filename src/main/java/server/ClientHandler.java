package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;

    private String username;

    private static final Map<PrintWriter, String> clients = Collections.synchronizedMap(new HashMap<>());

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.username = "Гость_" + socket.getPort();
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (PrintWriter client : clients.keySet()) {
                client.println(message);
            }
        }
    }

    @Override
    public void run() {
        try (
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                Scanner input = new Scanner(inputStream)
        ) {
            out = new PrintWriter(outputStream, true);
            clients.put(out, username);
            out.println("Добро пожаловать в чат!");

            while (input.hasNextLine()) {
                String message = input.nextLine().trim();

                // команда смены имени /name
                if (message.startsWith("/name")) {
                    String newUsername = message.substring(6).trim();

                    if (!newUsername.isEmpty() && !newUsername.contains("/")) {
                        broadcast(username + " теперь известен как '" + newUsername + "'!");
                        username = newUsername;
                        clients.put(out, username); // обновляем имя в мапе
                        out.println("Новое имя: " + newUsername);
                    } else {
                        out.println("Имя не может быть пустым или содержать '/'");
                    }
                    continue;
                }

                // команда выхода из чата /exit
                if (message.equalsIgnoreCase("/exit")) {
                    out.println("Вы вышли из чата.");
                    broadcast(username + " покинул чат.");
                    break;
                }

                // команда отображения подключенных клиентов /users
                if (message.equalsIgnoreCase("/users")) {
                    StringBuilder userList = new StringBuilder("Сейчас подключены:");
                    synchronized (clients) {
                        for (String name : clients.values()) {
                            userList.append("\n• ").append(name);
                        }
                    }
                    out.println(userList.toString());
                    continue;
                }

                // обычное сообщение
                broadcast(username + ": " + message);
            }
        } catch (IOException exception) {
            System.err.println("Клиент отключился: " + exception.getMessage());
        } finally {
            if (out != null) {
                clients.remove(out);
            }
            try {
                socket.close();
            } catch (IOException ignore) {}
        }
    }
}
