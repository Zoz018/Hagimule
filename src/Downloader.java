import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Downloader extends Thread {

    private final String client; // Adresse du client (daemon)
    private final String fileName; // Nom du fichier demandé
    private final long start; // Début du fragment
    private final long end; // Fin du fragment
    private final RandomAccessFile outputFile; // Fichier de sortie pour écrire les fragments
    private static final String FILE_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/FichierTest";
    private static final String TL_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/FichierTL"; 

    /**
     * Constructeur du Downloader (thread) pour télécharger un fragment.
     *
     * @param client     Adresse du daemon qui possède le fragment
     * @param fileName   Nom du fichier demandé
     * @param start      Début du fragment dans le fichier
     * @param end        Fin du fragment dans le fichier
     * @param outputFile Fichier de sortie pour écrire les données
     */
    public Downloader(String client, String fileName, long start, long end, RandomAccessFile outputFile) {
        this.client = client;
        this.fileName = fileName;
        this.start = start;
        this.end = end;
        this.outputFile = outputFile;
    }

    /**
     * Méthode exécutée par chaque thread pour télécharger un fragment du fichier.
     */
    @Override
    public void run() {
        try (Socket socket = new Socket(client, Daemon.PORT); // Crée une connexion TCP avec le daemon
             DataInputStream in = new DataInputStream(socket.getInputStream()); // Prépare un flux pour recevoir les données
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) { // Prépare un flux pour envoyer la requête

            // Envoie la requête au daemon
            out.writeUTF(fileName); // Nom du fichier demandé
            out.writeLong(start); // Début du fragment
            out.writeLong(end); // Fin du fragment

            // Vérifie si le fragment est disponible
            if (!in.readBoolean()) {
                System.err.println("Fragment non disponible depuis le client : " + client);
                return;
            }

            // Lecture des données reçues
            int fragmentSize = in.readInt(); // Taille réelle du fragment envoyé
            byte[] buffer = new byte[fragmentSize]; // Tampon pour stocker les données
            in.readFully(buffer); // Lit exactement le nombre d'octets attendu

            // Écriture dans le fichier local à l'emplacement approprié
            synchronized (outputFile) { // Synchronisation pour éviter des conflits lors de l'écriture
                outputFile.seek(start); // Place le curseur au bon emplacement
                outputFile.write(buffer); // Écrit le fragment dans le fichier
            }

            System.out.println("Fragment téléchargé depuis " + client + " [start=" + start + ", size=" + fragmentSize + ", end="+end+"]");

        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement depuis le client " + client);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Downloader <fileName> <outputPath>");
            return;
        }

        String fileName = args[0]; // Nom du fichier à télécharger
        String outputPath = args[1]; // Chemin pour le fichier de sortie
        String diary_addr = args[2];

        try {
            // Recherche l'annuaire RMI
            DiaryInterface diary = (DiaryInterface) Naming.lookup("rmi://"+diary_addr+"/Annuaire");

            // Ajout du chrono : capture de l'heure de début
            long startTime = System.nanoTime();
            
            // Récupère la liste des clients possédant le fichier
            List<String> clients = diary.getClients(fileName);

            if (clients.isEmpty()) {
                System.out.println("Aucun client ne possède le fichier demandé.");
                return;
            }

            // Récupère la taille totale du fichier
            long fileSize = diary.getFileSize(fileName);

            // Ouvre ou crée le fichier de sortie avec la taille appropriée
            RandomAccessFile outputFile = new RandomAccessFile(outputPath+"/"+fileName, "rw");
            outputFile.setLength(fileSize); // Réserve l'espace nécessaire

            // Calcule la taille des fragments
            long fragmentSize = fileSize / clients.size();

            // Liste pour stocker les threads
            List<Thread> threads = new ArrayList<>();

            // Lance un thread pour chaque fragment
            for (int i = 0; i < clients.size(); i++) {
                long start = i * fragmentSize;
                long end = (i == clients.size() - 1) ? fileSize : (start + fragmentSize); // Dernier fragment ajuste la taille

                // Création et démarrage du thread Downloader
                Downloader downloader = new Downloader(clients.get(i), fileName, start, end, outputFile);
                threads.add(downloader); // Ajoute le thread à la liste
                downloader.start(); // Démarre le thread
            }

            // Attendre que tous les threads soient terminés
            for (Thread thread : threads) {
                thread.join(); // Bloque jusqu'à la fin du thread
            }

            long endTime = System.nanoTime();
            long tempsTelechargement = endTime - startTime;
            System.out.println("Temps écoulé pour télécharger le fichier : " + tempsTelechargement / 1_000_000 + " ms");

            if (outputFile.length() == fileSize) {
                System.out.println("Fichier téléchargé dans son intégralité !");
            } else {
                System.out.println("Erreur lors du téléchargement : le fichier n'est pas téléchargé dans son intégralité ... ");
            }


        } catch (Exception e) {
            System.err.println("Erreur lors du téléchargement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}