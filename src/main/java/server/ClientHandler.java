package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;

    private static final List<PrintWriter> clients = Collections.synchronizedList(new ArrayList<>());

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }

    @Override
    public void run() {
        try (
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                Scanner input = new Scanner(inputStream);
        ) {
            out = new PrintWriter(outputStream, true);
            clients.add(out);
            out.println("Добро пожаловать в чат!");

            while (input.hasNextLine()) {
                String message = input.nextLine();
                broadcast("Клиент " + socket.getPort() + ": " + message);
            }
        }
        catch (IOException exception) {
            System.err.println("Клиент отключился: " + exception.getMessage());}

        finally {
            try {socket.close();}
            catch (IOException ignore) {}
        }
    }
}