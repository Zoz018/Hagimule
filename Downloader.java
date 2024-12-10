import java.io.*;
import java.net.*;
import java.util.concurrent.*;


public class Downloader {
    //ExecutorService pour gérer les threads (ici 4 threads à changer selon les performances de Hagimule)
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    //Méthode pour télécharger un fichier à partir de plusieurs clients
    public void download(String[] clients, String fileName, long fileSize) throws IOException {

        //Calcul de la taille de chaque fragment
        long fragmentSize = fileSize / clients.length;
        try {
            for (int i = 0; i < clients.length; i++) {
                //On applique un décallage à chaque itération pour télécharger la bonne partie du fichier
                long offset = i * fragmentSize;
                //Calcul de la taille du fragment que l'on doit télécharger
                long length;
                //On regarde si le client est le dernier de la liste
                if (i == clients.length - 1){
                    //Si oui la taille du dernier fragment n'est surement pas de la taille des autres fragments
                    length = (fileSize - offset) ;
                }else{
                    //Sinon on utilise la même taille que les autres fragments
                    length = fragmentSize;
                }
                
                //On stocke le numéro du client courant
                int finalI = i;
                //Pour chaque client on télécharge le fragment.
                executor.submit(() -> downloadFragments(clients[finalI], fileName, offset, length));
            }

            //Pour que l'executor ne prennent plus de tâche j'usqu'à la fin des autres tâches
            executor.shutdown();
            //On attend que les tâches soient terminé ou que le temps soit terminé (ici 1 heure (à modifier surement))
            executor.awaitTermination(1, TimeUnit.HOURS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }  

    public void downloadFragments(String client, String fileName, long offset, long length) {
            try (Socket socket = new Socket(client, Daemon.PORT);
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
        
                // Envoi de la requête au daemon
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(fileName);
                out.writeLong(offset);
                out.writeInt(Math.toIntExact(length));
    
            // Vérifier si le fichier est accessible
            if (!in.readBoolean()) {
                System.err.println("Fragment non disponible");
                return;
            }
    
            // Lecture des données reçues
            int fragmentSize = in.readInt();
            byte[] buffer = new byte[fragmentSize];
            in.readFully(buffer);
    
            // Écriture directe dans le fichier à l'emplacement approprié
            raf.seek(offset);
            raf.write(buffer);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}

