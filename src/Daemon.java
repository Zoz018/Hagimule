import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.logging.FileHandler;

public class Daemon {
    public static final int PORT = 8080;
    private static final String FILE_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/FichierTest"; // Dossier contenant les fichiers locaux du démon.
    private final Integer PACKET_SIZE = 1024; // in byte
    private final Integer LATENCY = 10; // in usec

    public Daemon(){
    }

     /**
     * Méthode pour démarrer le daemon et écouter les requêtes sur le port spécifié.
     * @param port Le port sur lequel le daemon doit écouter.
     */
    public void run(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) { 
            System.out.println("Daemon is running on port " + port);

            while (true) { 
                Socket clientSocket = serverSocket.accept(); // Attend une connexion client
                System.out.println("Connection established with client: " + clientSocket.getInetAddress());
                
                new Thread(() -> handleClient(clientSocket)).start(); // Crée un thread pour gérer chaque client individuellement
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour gérer les requêtes des clients.
     * @param clientSocket La connexion avec le client.
     */
    private void handleClient(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            // Lecture des informations sur la requête
            String fileName = in.readUTF(); // Récupère le nom du fichier demandé
            long start = in.readLong(); // Récupère le début du fragment demandé
            long end = in.readLong(); // Récupère la fin du fragment demandé

            File file = new File(FILE_DIRECTORY, fileName); // Chemin complet du fichier demandé

            if (!file.exists() || !file.isFile()) {
                out.writeBoolean(false); // Si le fichier n'existe pas, retourne une erreur au client
                System.err.println("File not found: " + fileName);
                return;
            }

            // Indique que le fichier est disponible
            out.writeBoolean(true);

            // Lis le fragment demandé dans le fichier
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(start); // Place le curseur au bon endroit dans le fichier

                int nbBit = (int) (end-start);
                int nbBitSecondaire = nbBit;
                int nbBitLu = 0;

                byte[] buffer = new byte[nbBit];

                out.writeInt(nbBit); // Envoie la taille du fragment lu au client

                while (nbBitSecondaire > 0) {

                    int nbBitRecupere = (int) Math.min(PACKET_SIZE, nbBitSecondaire);

                    nbBitLu = raf.read(buffer, 0, nbBitRecupere);

                    if (nbBitLu == -1) {
                        break; // Fin du fichier atteinte
                    }

                    out.write(buffer, 0, nbBitLu);
                    nbBitSecondaire -= nbBitLu;

                    // Attendre le délai spécifié
                    try {
                        Thread.sleep(LATENCY); // en millisecondes
                    } catch (InterruptedException e) {
                        System.err.println("Sleep interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Réinterrompre le thread pour respecter l'état d'interruption
                    }
            
                }

                System.out.println("Sent fragment to client: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close(); // Ferme la connexion avec le client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: java Daemon <RMIHost> <ClientName>");
            return;
        }

        try {
            String rmiHost = args[0].trim(); // Adresse du serveur RMI
            String clientName = args[1].trim(); // Nom de la machine locale comme identifiant du client

            DiaryInterface diary = (DiaryInterface) Naming.lookup("//" + rmiHost + "/Annuaire"); // Connexion à l'annuaire RMI

            File folder = new File(FILE_DIRECTORY); // Ouvre le dossier contenant les fichiers du client

            File[] files = folder.listFiles(); // Liste les fichiers dans le dossier

            if (files != null) { // Vérifie que le dossier contient des fichiers
                for (File file : files) {
                    if (file.isFile()) { // Vérifie que l'élément est bien un fichier
                        diary.registerFile(clientName, file.getName(), file.length()); // Enregistre le fichier dans l'annuaire
                        System.out.println("Registered file: " + file.getName());
                    }
                }
            }

            System.out.println("Daemon " + clientName + " est enregistré.");

            // Lance le daemon pour écouter les requêtes sur le port spécifié
            Daemon daemon = new Daemon();
            daemon.run(PORT);
            //

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

    

    

