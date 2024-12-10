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
                executor.submit(() -> downloadFragment(clients[finalI], fileName, offset, length));
            }

            //Pour que l'executor ne prennent plus de tâche j'usqu'à la fin des autres tâches
            executor.shutdown();
            //On attend que les tâches soient terminé ou que le temps soit terminé (ici 1 heure (à modifier surement))
            executor.awaitTermination(1, TimeUnit.HOURS);

            //Fusion des fragments pour avoir le fichier complet
            mergeFragments(fileName, clients.length);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }  
}

