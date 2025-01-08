import java.rmi.Naming; // Pour rechercher l'annuaire RMI.
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile; // Pour manipuler un fichier de sortie.
import java.net.Socket;
import java.util.List; // Pour gérer des listes.

public class Downloader extends Thread {

    //crée dowloader pour chaque fragment qui se lance avec run (remplace dowlaodfrgmet) (thread.start())
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Downloader <fileName> <outputPath>");
            return;
        }

        String fileName = args[0]; // Récupère le nom du fichier à télécharger
        String outputPath = args[1]; // Récupère le chemin du fichier de sortie

        try {
            DiaryInterface diary = (DiaryInterface) Naming.lookup("rmi://localhost/DiaryService"); // Recherche l'annuaire RMI

            List<String> clients = diary.getClients(fileName); // Récupère la liste des clients possédant le fichier

            if (clients.isEmpty()) {
                System.out.println("No clients have the requested file.");
                return; // Si aucun client ne possède le fichier, on quitte
            }

            long fileSize = diary.getFileSize(fileName); // Récupère la taille du fichier

            long fragmentSize = fileSize / clients.size(); // Calcule la taille de chaque fragment à télécharger

            RandomAccessFile outputFile = new RandomAccessFile(outputPath, "rw"); // Crée un fichier de sortie en mode lecture/écriture

            outputFile.setLength(fileSize); // Réserve l'espace nécessaire pour le fichier complet

            for (int i = 0; i < clients.size(); i++) {
                long start = i * fragmentSize;
                long end = (i == clients.size() - 1) ? fileSize : (start + fragmentSize); // Détermine les limites de chaque fragment

                // Téléchargement en thread
                final String client = clients.get(i);
                new Thread(() -> {
                    try {
                        downloadFragments(client, fileName, start, end);
                    } catch (Exception e) {
                        System.err.println("Erreur avec le client " + client + ": " + e.getMessage());
                    } finally {
                        latch.countDown(); // Réduit le compteur une fois terminé
                    }
                }).start();
            }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Donner outputfile pour écrire e U'ON VA RECEOIR, STRAT END OK, FILENAME POUR DEMANDER AUX DAEMONS, client pour avoir la'dresse pour démarrer téléchargemeny

    public void downloadFragments(String client, String fileName, long offset, long length) {
            try (Socket socket = new Socket(client, Daemon.PORT); //Crée une connexion TCP avec le daemon
                 DataInputStream in = new DataInputStream(socket.getInputStream()); //Prépare un flux pour recvoir les données du daemon
                 RandomAccessFile raf = new RandomAccessFile(fileName, "rw")){ //Ouvre le fichier local 
        
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
            byte[] buffer = new byte[fragmentSize]; //il faut envoyer pile fragmentSize sinon on reste bloqué
            in.readFully(buffer);
    
            // Écriture directe dans le fichier à l'emplacement approprié
            raf.seek(offset); //déplace le curseur jusqu'a l'offset
            raf.write(buffer); //ecrire ce qu'il a dans le buffer

    //gérer si erreur dans la reception (voir dans readFully)

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


