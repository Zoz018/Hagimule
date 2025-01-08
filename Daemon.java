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

    public void run(int port) {
        //ecouter sur le port si il y a une requete et la traiter
    }

    public static void main(String[] args) {
        try {

            DiaryInterface diary = (DiaryInterface) Naming.lookup("rmi://"+args[0]+"/DiaryService"); // Recherche l'annuaire RMI via son nom "DiaryService".

            String clientName = args[1]; // Récupère le nom de la machine locale comme identifiant du client.

            File folder = new File(FILE_DIRECTORY); // Ouvre le dossier contenant les fichiers du client.

            File[] files = folder.listFiles(); // Liste les fichiers dans le dossier.

            if (files != null) { // Vérifie que le dossier contient des fichiers.
                for (File file : files) {
                    if (file.isFile()) { // Vérifie que l'élément est bien un fichier.
                        diary.registerFile(file.getName(), clientName, file.length()); // Enregistre le fichier dans l'annuaire.
                        System.out.println("Registered file: " + file.getName());
                    }
                }
            }

            System.out.println("Daemon " + clientName + " is registered.");

            Daemon D = new Daemon;
            D.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    
}
