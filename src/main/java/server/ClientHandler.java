package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                Scanner scanner = new Scanner(is);
                PrintWriter out = new PrintWriter(os, true)
        )
        {
            out.println("Добро пожаловать в чат!");
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                System.out.println("Получено сообщение: " + message);
                out.println("Эхо: " + message);}
        }

        catch (IOException exception) {
            System.err.println("Клиент отключился: " + exception.getMessage());}

        finally {
            try {socket.close();}
            catch (IOException ignore) {}
        }
    }
}