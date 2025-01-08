import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.RemoteException;

public class DiaryImpl extends UnicastRemoteObject implements DiaryInterface {
    //Annuaire de la forme : fileMap = { "fileName1" : ([Client1] , taille), "fileName2" : ([Client1, Client2], taille) }
    public final Map<String, Map.Entry<List<String>, Long>> fileRegistry;
    private ArrayList<String> allClients;


    public static void main(String[] args){
        try {
            //Initialisation de l'annuaire 
            DiaryImpl diary = new DiaryImpl();
            //Localisation ou création de l'annuaire sur le port 1099 et on enregistre l'objet diary sous le nom "Annuaire"
            LocateRegistry.createRegistry(1099).rebind("Annuaire", diary);
            //Debug 
            System.out.println("Le serveur de l'annuaire tourne...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
    }
    
    public DiaryImpl() throws RemoteException{
        super(); //Pour initialiser la super classe URO
        this.fileRegistry = new HashMap<>(); //Initialisation de L'annuaire 
    }

    @Override
    public void registerFile(String client, String fileName, Long fileSize) throws RemoteException {
        // Vérifie si le fichier existe déjà dans l'annuaire
        if (fileRegistry.containsKey(fileName)) {
            List<String> ClientList = new ArrayList<>();
            ClientList = getClients(fileName);
            ClientList.add(client); // Ajoute le client actuel
            fileRegistry.put(fileName, new AbstractMap.SimpleEntry<>(ClientList, fileSize));
        } else {
            // Si le fichier n'existe pas, on crée une nouvelle entrée
            List<String> newClientList = new ArrayList<>();
            newClientList.add(client); // Ajoute le client actuel
            fileRegistry.put(fileName, new AbstractMap.SimpleEntry<>(newClientList, fileSize));
        }
    }

    @Override
    public List<String> getClients(String fileName) {
        // Retourne la liste des clients pour un fichier donné, ou une liste vide si le fichier n'existe pas.
        return fileRegistry.containsKey(fileName) ? fileRegistry.get(fileName).getKey() : new ArrayList<>();
    }

    @Override
    public List<String> getAllClients() throws RemoteException {
        Set<String> allClients = new HashSet<>();  // Utilise un Set pour éviter les doublons.

        // Ajoute tous les clients de chaque fichier à la collection.
        for (Map.Entry<List<String>, Long> entry : fileRegistry.values()) {
            allClients.addAll(entry.getKey());
        }
        return new ArrayList<>(allClients); // Convertit le Set en liste avant de le retourner
    }

    @Override
    public long getFileSize(String fileName) throws RemoteException {
        if (fileRegistry.containsKey(fileName)) { 
            return fileRegistry.get(fileName).getValue(); 
        } else {
            throw new IllegalArgumentException("Le fichier '" + fileName + "' n'existe pas dans l'annuaire.");
        }
    }
   
}   
