import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import java.util.*;

public class DiaryImpl extends UnicastRemoteObject implements DiaryInterface {
    //Annuaire de la forme : fileMap = { "fileName1" : [Client1] , "fileName2" : [Client1, Client2] }
    private final Map<String,List<String>> fileMap;
    
    public DiaryImpl() throws RemoteException{
        super(); //Pour initialiser la super classe URO
        this.fileMap = new HashMap<>(); //Initialisation de L'annuaire 
    }

    //Les méthodes sont synchronisées dans le cas où plusieurs clients changent ou accèdent à l'annuaire en même temps par exemple 
    @Override
    public synchronized void registerFile(String clientID, String fileName) throws RemoteException {
        //On ajoute le fichier au client si le fichier existe sinon on met en place une liste vide qui contiendra les clients
        fileMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(clientID);
        //Debugage
        System.out.println("Le fichier " + fileName + " à été enregistrer par le client " + clientID);
    }

    @Override
    public synchronized List<String> getClient(String fileName) throws RemoteException {
        //On renvoie les noms des clients qui possède le fichier et une liste vide sinon
        return fileMap.getOrDefault(fileName , Collections.emptyList());
    }
   
}   
