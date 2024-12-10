import java.io.*;
import java.net.*;
import java.util.concurrent.*;


public class Downloader {
    //ExecutorService pour gérer les threads (ici 4 threads à changer selon les performances de Hagimule)
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    //Méthode pour télécharger un fichier à partir de plusieurs clients
    public void download(String[] clients, String fileName, long fileSize) {
        //Calcul de la taille de chaque fragment
        long fragmentSize = fileSize / clients.length;
    }
}

