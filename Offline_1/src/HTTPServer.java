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
                if (requestLine == null) {
                    sendErrorResponse(out, "400 Bad Request", "Invalid HTTP request");
                    return;
                }

                String[] reqSegments = requestLine.split(" ");
                if (reqSegments[0].equals("GET")) {
//                    System.out.println(requestLine);
                    handleGetRequest(reqSegments, out, fileOut);
                } else if (reqSegments[0].equals("UPLOAD")) {
                    handleUploadRequest(reqSegments, in);
                } else {
                    out.println("HTTP/1.0 400 Bad Request");
                }

        } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendErrorResponse(PrintWriter out, String status, String message)
        {
            System.out.println("Error: " + status);
            out.println("HTTP/1.0 " + status);
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>ERROR</title></head><body><h1>" + message + "</h1></body></html>");
        }

        private void handleGetRequest(String[] reqSegments, PrintWriter out, OutputStream fileOut)
        {

        }

        private void handleUploadRequest(String[] reqSegments, BufferedReader in)
        {

        }

    }

}
