import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import java.util.*;

public class DiaryImpl extends UnicastRemoteObject implements DiaryInterface {
    //Annuaire de la forme : fileMap = { "fileName1" : ([Client1] , taille), "fileName2" : ([Client1, Client2], taille) }
    public final Map<String, Map.Entry<List<String>, Long>> fileRegistry;
    
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
