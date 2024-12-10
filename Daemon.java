import java.io.*;
import java.net.*;

public class Daemon {
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        //Initialisation du serveur sur le port 8080
        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            //Debug
            System.out.println("Daemon en cours ... " + PORT);
            while (true) {
                //Attente de connexion d'un client
                Socket socket = serverSocket.accept();
                //Démarrage d'un thread pour gérer la connexion avec le client 
                new Thread(new FileHandler(socket)).start();
            }        
        } catch (IOException e){
            e.printStackTrace();
        }
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
            //Lecture du decalage 
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

                //Envoi du fichier au client avec la taille du fichier
                out.writeInt(buffer.length);
                out.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
}