import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.logging.FileHandler;

public class Daemon {
    public static final int PORT = 1099;
    public static void main(String[] args) {
        Daemon daemon = new Daemon();
        daemon.start();
}

    public void start(){
         //Initialisation du serveur sur le port 1099
         try (ServerSocket serverSocket = new ServerSocket(PORT)){
            //Debug
            System.out.println("Daemon en cours ... " + PORT);
            while (true) {
                //Attente de connexion d'un client
                Socket socket = serverSocket.accept();
                //Démarrage d'un thread pour gérer la connexion avec le client 
                FileHandler fileHandler = new FileHandler(socket);
                new Thread(fileHandler).start();
            }        
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void registerCustomer(){
        // Connexion à l'annuaire RMI qui se trouve sur le port 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        // Recherche de l'objet "Annuaire" dans l'annuaire RMI
            DiaryImpl diary = (DiaryImpl) registry.lookup("Annuaire");
        // Récupérer les valeurs dans le diary
        for (Map.Entry<String, String> entry : .entrySet()) {
            String value = entry.getValue();
    }

// Pour le client, on peut utiliser le code suivant pour envoyer un fichier au serveur
class FileHandler implements Runnable {
    private final Socket socket;

    //Constructeur
    public FileHandler(Socket socket) {
        this.socket = socket;
    }

    //Méthode run qui sera exécutée
    @Override
    public void run() {
        //Initialisation des flux d'entrée et de sortie
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            
            //Lecture du nom du fichier
            String fileName = in.readUTF();
            //Lecture du decalage (point de départ de lecture)
            long offset = in.readLong();
            //Lecture de la longueur du fichier
            int length = in.readInt();

            //Création d'un objet File et vérification de l'existence du fichier
            File file = new File(fileName);
            if (!file.exists()) {
                out.writeBoolean(false);
                return;
            }
            out.writeBoolean(true);

            //Lecture du fichier et envoi au client
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                //Création d'un buffer pour stocker les données de la taille du fichier
                byte[] buffer = new byte[length];
                //Déplacement du curseur à l'endroit spécifié
                raf.seek(offset);
                //Lecture du fichier
                raf.read(buffer);

                //Envoie de la taille du fichier au client et du fichier 
                out.writeInt(buffer.length);
                out.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
}