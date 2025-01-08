import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.logging.FileHandler;

public class Daemon {
    public static final int PORT = 8080;
    private static final String FILE_DIRECTORY = "dossierFichierTest"; // Dossier contenant les fichiers locaux du démon.

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

                byte[] buffer = new byte[((int) (end-start))];
                int bytesRead = raf.read(buffer); // Lit le fragment demandé

                out.writeInt(bytesRead); // Envoie la taille du fragment lu au client
                out.write(buffer, 0, bytesRead); // Envoie les données au client

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
            String rmiHost = args[0]; // Adresse du serveur RMI
            String clientName = args[1]; // Nom de la machine locale comme identifiant du client

            DiaryInterface diary = (DiaryInterface) Naming.lookup("rmi://" + "1099" + "/DiaryService"); // Connexion à l'annuaire RMI

            File folder = new File(FILE_DIRECTORY); // Ouvre le dossier contenant les fichiers du client

            File[] files = folder.listFiles(); // Liste les fichiers dans le dossier

            if (files != null) { // Vérifie que le dossier contient des fichiers
                for (File file : files) {
                    if (file.isFile()) { // Vérifie que l'élément est bien un fichier
                        diary.registerFile(file.getName(), clientName, file.length()); // Enregistre le fichier dans l'annuaire
                        System.out.println("Registered file: " + file.getName());
                    }
                }
            }

            System.out.println("Daemon " + clientName + " is registered.");

            // Lance le daemon pour écouter les requêtes sur le port spécifié
            Daemon daemon = new Daemon();
            daemon.run(PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

    

    

