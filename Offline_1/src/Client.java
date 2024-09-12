import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5014;
    private static final int CHUNK_SIZE = 4096; // Size of chunks in bytes
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("txt", "jpg", "png", "mp4");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Vector<Thread> threads = new Vector<>();
        while (true) {
            System.out.print("Enter the file name to upload (or type 'exit' to quit): ");
            String fileName = scanner.nextLine();

            if (fileName.equalsIgnoreCase("exit")) {
                for (Thread thread : threads) {
                    try {
                        thread.join();  // This will wait for the thread to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Client is shutting down...");
                break;
            }
            else {
                Thread t = new Thread(new FileUploader(fileName));
                threads.add(t);
                t.start();
            }
        }
        scanner.close();
    }

    private static class FileUploader implements Runnable {
        private final String fileName;

        public FileUploader(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                System.out.println("Connected to server");

                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    System.out.println("Invalid file.");
                    socket.close();
                    return;
                }

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String [] parts = fileName.split("/");
                out.println("UPLOAD " + parts[parts.length - 1]);

                if(!isAllowedFile(file.getName())) {
                    System.out.println("ERROR! File type not allowed.");
                    socket.close();
                    return;
                }

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                bis.close();
                bos.close();
                socket.close();
                System.out.println("File uploaded successfully.");

            } catch (Exception e) {
                e.printStackTrace();
            }
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
