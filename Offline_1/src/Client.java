import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5014;
    private static final int CHUNK_SIZE = 128; // Size of chunks in bytes
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("txt", "jpg", "png", "mp4");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Vector<Thread> threads = new Vector<>();
        System.out.print("Enter the file name to upload (or type 'exit' to quit): ");
        while (true) {
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
        private String fileName;

        public FileUploader(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            try {
                if(fileName.startsWith("/"))
                    fileName = fileName.substring(1);
                Path path = Paths.get(fileName);
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        System.out.println("Can't upload a directory.");
                        return;
                    }
                }
                else {
                    System.out.println("File not found.");
                    return;
                }

                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                FileInputStream fis = new FileInputStream(path.toFile());
                String [] parts = fileName.split("/");
                String name = parts[parts.length - 1];
                if(name.contains(" ")) {
                    String [] nameParts = name.split(" ");
                    name = String.join("_", nameParts);
                }
                String uploadCommand = "UPLOAD " + name + "\n";
                dos.writeBytes(uploadCommand);
                dos.flush();

                if(!isAllowedFile(fileName)) {
                    System.out.println("ERROR! File type not allowed.");
                    socket.close();
                    return;
                }

                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                    dos.flush();
                }
                fis.close();
                dos.close();
//                Thread.sleep(1000);
                socket.close();
                System.out.println(fileName + " uploaded successfully.");

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

//   /ROOT/vedio.mp4
//   lecture.mp4