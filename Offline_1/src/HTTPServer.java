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
    private static final int CHUNK_SIZE = 4096;
    private static final String ROOT_DIR = "ROOT";
//    private static final String ROOT_DIR = ".";
    private static final Logger logger = Logger.getLogger(HTTPServer.class.getName());

    public static void main(String[] args) {
//        setupLogger();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);
            new Thread(new serverConsole()).start();
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(new RequestHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server is shutting down...");
        }
    }

    private static class serverConsole implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.nextLine();
                if (command.equals("exit")) {
                    System.out.println("Server is shutting down...");
                    System.exit(0);
                }
            }
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
                System.out.println(requestLine);
                if (requestLine == null) {
                    sendErrorResponse(out, "400: Bad Request", "Invalid HTTP request");
                    return;
                }

                String[] reqSegments = requestLine.split(" ");
                if (reqSegments[0].equals("GET")) {
                    handleGetRequest(reqSegments, out, fileOut);
                } else if (reqSegments[0].equals("UPLOAD")) {
                    handleUploadRequest(reqSegments, in);
                } else {
                    sendErrorResponse(out, "400: Bad Request", "Invalid HTTP request");
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
            out.println("<html><head><title>ERROR!</title></head><body><h1>" + message + "</h1></body></html>");
        }

        private void handleGetRequest(String[] reqSegments, PrintWriter out, OutputStream fileOut) throws IOException {
            if (reqSegments.length != 3) {
                sendErrorResponse(out, "400: Bad Request", "Invalid HTTP request format");
                return;
            }
            String filePath = reqSegments[1];
            if (filePath.equals("/")) {
                filePath = "/ROOT";
            }
            filePath = filePath.substring(1);
//            System.out.println(filePath);

//            Path path = Paths.get(ROOT_DIR, filePath).normalize();
            Path path = Paths.get(filePath);
//            System.out.println(path);
//            System.out.println(path2);
//            System.out.println(path.getFileName());
            if (Files.exists(path)) {
//                System.out.println("File exists");
                if (Files.isDirectory(path)) {
//                    System.out.println("File is a directory");
                    sendDirectoryResponse(out, path);
                } else {
//                    System.out.println("File is a file");
                    sendFileResponse(out, path, fileOut);
                }
            } else {
                sendErrorResponse(out, "404: Not Found", "File not found");
            }

//            logger.info("Request: " + Arrays.toString(reqSegments) + " | Response: " + (Files.exists(file) ? "200 OK" : "404 Not Found"));
        }

        private void sendFileResponse(PrintWriter out, Path file, OutputStream fileOut) throws IOException {
            String mimeType = Files.probeContentType(file);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            String [] mime = mimeType.split("/");
//            System.out.println(mime[0]);

            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: " + mimeType);
            out.println("Content-Length: " + Files.size(file));
            if(!mime[0].equals("image") && !mime[0].equals("text")) {
                out.println("Content-Disposition: attachment; filename=\"" + file.getFileName().toString() + "\"");
            }
            out.println();

            try (FileInputStream fis = new FileInputStream(file.toFile())) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendDirectoryResponse(PrintWriter out, Path dir) throws IOException {
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println();

            out.println("<html><body>");
            out.println("<h1>Directory Listing</h1>");
            out.println("<ul>");

            try (Stream<Path> paths = Files.list(dir)) {
                for (Path path : paths.toList()) {
                    String link = path.getFileName().toString();
                    if (Files.isDirectory(path)) {
                        link = "<b><i>" + link + "</i></b>";
                        out.println("<li><a href=\"" + dir.getFileName()+"/"+path.getFileName() + "\">" + link + "</a></li>");
                    }
                    else{
                        out.println("<li><a href=\"" + dir.getFileName()+"/"+path.getFileName() + "\" target=\"_blank\">" + link + "</a></li>");
                    }
                }
            }

            out.println("</ul>");
            out.println("</body></html>");
        }

        private void handleUploadRequest(String[] reqSegments, BufferedReader in) throws FileNotFoundException {
            String fileName = reqSegments[1];
            File file = new File(ROOT_DIR + "/uploaded/" + fileName);

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                char[] buffer = new char[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    bos.write(new String(buffer, 0, bytesRead).getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("File uploaded: " + file.getName());
        }

    }

}
