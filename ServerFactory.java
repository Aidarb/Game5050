package aidarb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFactory {

    public static final int PORT = 8080;
    public static int numOfPools = 30;

    public static void main(String[] args) throws IOException {

        int id = 1;
        ServerSocket server = new ServerSocket(PORT);
        ExecutorService executorService = Executors.newFixedThreadPool(numOfPools);

        try {
            while (!server.isClosed()) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                System.out.println("Клиент " + id + " подключен");
                executorService.execute(new Server(id, socket));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            server.close();
        }
    }
}