import java.io.*;
import java.net.*;
import java.util.*;

public class Downloader {
    //ExecutorService pour gérer les threads (ici 4 threads à changer selon les performances de Hagimule)
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    //Méthode pour télécharger un fichier à partir de plusieurs clients
    public void download(String[] clients, String fileName, long fileSize) {
        //Calcul de la taille de chaque fragment
        long fragmentSize = fileSize / clients.length;

        try {
            for (int i = 0; i < clients.length; i++) {
                long offset = i * fragmentSize;
                long length = (i == clients.length - 1) ? fileSize - offset : fragmentSize;

                int finalI = i;
                executor.submit(() -> downloadFragment(clients[finalI], fileName, offset, length));
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

            mergeFragments(fileName, clients.length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void downloadFragment(String client, String fileName, long offset, long length) {
        try (Socket socket = new Socket(client, 8080);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF(fileName);
            out.writeLong(offset);
            out.writeInt((int) length);

            boolean exists = in.readBoolean();
            if (!exists) {
                System.out.println("File not found on client: " + client);
                return;
            }

            int fragmentLength = in.readInt();
            byte[] buffer = new byte[fragmentLength];
            in.readFully(buffer);

            try (FileOutputStream fos = new FileOutputStream(fileName + ".part" + offset)) {
                fos.write(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeFragments(String fileName, int parts) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            for (int i = 0; i < parts; i++) {
                try (FileInputStream fis = new FileInputStream(fileName + ".part" + (i * (1L << 20)))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}

