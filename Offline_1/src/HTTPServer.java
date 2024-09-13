import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class HTTPServer {
    private static final int SERVER_PORT = 5014;
    private static final String UPD_DIR = "uploaded";
    private static final int CHUNK_SIZE = 4096;
    private static final String ROOT_DIR = "ROOT";
    private static final String LOG_FILE = "log.txt";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("txt", "jpg", "png", "mp4");

    public static void main(String[] args) {

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

    public static class Logger {

        // Synchronized method to write logs
        public static synchronized void writeLog(String date, String request, String response) {
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write( "Date: "+date + "\nRequest: " + request + "\nResponse: " + response + "\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try (InputStream is = socket.getInputStream();
                 DataInputStream dis = new DataInputStream(is);
//                InputStreamReader isr = new InputStreamReader(is);
                 PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                 OutputStream fileOut = socket.getOutputStream()) {

//                StringBuilder request = new StringBuilder();
//                int data;
//                while ((data = isr.read()) != -1) {
//                    char character = (char) data;
//                    if (character == '\n') { // End of the request line
//                        break;
//                    }
//                    request.append(character);
//                }
//                String requestLine = request.toString();
                String requestLine = dis.readLine();
                System.out.println(requestLine);
                if (requestLine == null) {
//                    Logger.writeLog(new Date().toString(), requestLine, "400: Bad Request");
//                    sendErrorResponse(pw, "400 Bad Request", "Invalid HTTP request");
                    return;
                }

                String[] reqSegments = requestLine.split(" ");
                if (reqSegments[0].equals("GET")) {
                    try {
                        handleGetRequest(reqSegments, pw, fileOut);
                    } catch (SocketException se) {
                        System.err.println("Client disconnected prematurely: " + se.getMessage());
                        Logger.writeLog(new Date().toString(), requestLine, "Broken Pipe Error\nClient disconnected during file transmission");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (reqSegments[0].equals("UPLOAD")) {
                    handleUploadRequest(reqSegments, new DataInputStream(is));
                } else {
                    Logger.writeLog(new Date().toString(), requestLine, "400: Bad Request");
                    sendErrorResponse(pw, "400 Bad Request", "Invalid HTTP request");
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

        private void sendErrorResponse(PrintWriter pw, String status, String message)
        {
            String content = "<!DOCTYPE HTML><html><head><title>ERROR!</title></head><body><h1>" + "ERROR " + status.substring(0, 3) + "!<hr/>" + message + "</h1></body></html>";
            System.out.println("Error: " + status);
            pw.println("HTTP/1.1 " + status);
            pw.println("Content-Type: text/html");
            pw.println("Content-Length: " + content.length());
            pw.println();
            pw.println(content);
            pw.flush();
        }

        private void handleGetRequest(String[] reqSegments, PrintWriter pw, OutputStream fileOut) throws IOException {
            if (reqSegments.length != 3) {
                Logger.writeLog(new Date().toString(), Arrays.toString(reqSegments), "400 Bad Request");
                sendErrorResponse(pw, "400 Bad Request", "Invalid HTTP request format");
                return;
            }
            String filePath = reqSegments[1];
            if (filePath.equals("/")) {
                filePath = "/ROOT";
            }
            filePath = filePath.substring(1);
            if(filePath.contains("%20"))
            {
                String [] pathSeg = filePath.split("%20");
                filePath = String.join(" ", pathSeg);
            }
            System.out.println("File path: " + filePath);
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    sendDirectoryResponse(pw, path);
                } else {
                    sendFileResponse(pw, path, fileOut);
                }
            } else {
                Logger.writeLog(new Date().toString(), Arrays.toString(reqSegments), "404: Not Found");
                sendErrorResponse(pw, "404 Not Found", "File not found");
            }
            Logger.writeLog(new Date().toString(), Arrays.toString(reqSegments), Files.exists(path) ? "200 OK" : "404 Not Found");
        }

        private void sendFileResponse(PrintWriter pw, Path path, OutputStream fileOut) throws IOException {
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            String [] mime = mimeType.split("/");
//            System.out.println(mime[0]);

            pw.println("HTTP/1.0 200 OK");
            pw.println("Content-Type: " + mimeType);
            pw.println("Content-Length: " + Files.size(path));
            if(!mime[0].equals("image") && !mime[0].equals("text")) {
                pw.println("Content-Disposition: attachment; filename=\"" + path.getFileName().toString() + "\"");
            }
            pw.println();
            System.out.println("response sent, now sending file: "+path.getFileName());
//            try (FileInputStream fis = new FileInputStream(path.toFile())) {
//                byte[] buffer = new byte[CHUNK_SIZE];
//                int bytesRead;
//                while ((bytesRead = fis.read(buffer)) != -1) {
//                    fileOut.write(buffer, 0, bytesRead);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            try (DataInputStream fis = new DataInputStream(new FileInputStream(path.toFile()))) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    fileOut.flush();
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

        private void handleUploadRequest(String[] reqSegments, DataInputStream dis) throws FileNotFoundException {
            String fileName = reqSegments[1];
            if (!isAllowedFile(fileName)) {
                Logger.writeLog(new Date().toString(), Arrays.toString(reqSegments), "403: Forbidden");
                System.out.println("ERROR! 403: Forbidden\n"+"File name: "+fileName+"\t\"File type not allowed\"");
                return;
            }
            File file = new File(ROOT_DIR + "/"+UPD_DIR+"/" + fileName);

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = dis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Logger.writeLog(new Date().toString(), Arrays.toString(reqSegments), "File uploaded!");
            System.out.println("File uploaded: " + file.getName());
        }

        private static boolean isAllowedFile(String fileName) {
            String fileExtension = getFileExtension(fileName).toLowerCase();
            return ALLOWED_EXTENSIONS.contains(fileExtension);
        }

        private static String getFileExtension(String fileName) {
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                return fileName.substring(lastDotIndex + 1);
            }
            return "";
        }

    }

}
