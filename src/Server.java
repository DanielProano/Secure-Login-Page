import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6060)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    Thread client1 = new Thread(clientHandler);
                    client1.start();
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            return;
        }
    }
}
