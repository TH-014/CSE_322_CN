import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.nio.file.attribute.*;
import java.util.logging.*;

public class HTTPServer {
    private static final int SERVER_PORT = 5014;
    private static final String UPD_DIR = "uploaded";
    private static final int CHUNK_SIZE = 1024;
    private static final String ROOT_DIR = ".";
    private static final Logger logger = Logger.getLogger(HTTPServer.class.getName());

    public static void main(String[] args) {
//        setupLogger();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new RequestHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class RequestHandler implements Runnable {

        private final Socket socket;

        public RequestHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 OutputStream fileOut = socket.getOutputStream()) {

                String requestLine = in.readLine();
                if (requestLine == null || (!requestLine.startsWith("GET") && !requestLine.startsWith("UPLOAD"))) {
                    //send error response
                    return;
                }
        } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
