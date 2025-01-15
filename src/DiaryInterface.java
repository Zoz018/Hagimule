import java.rmi.*;
import java.util.List;

//Interface RMI de l'annuaire 
public interface DiaryInterface extends Remote {
    //Méthode qui permet d'ajouter le nom d'un client lorsque celui-ci possède le fichier
    public void registerFile(String client, String fileName, Long fileSize) throws RemoteException; 

    //Méthode qui récupère la liste des client qui possède le fichier
    List<String> getClients(String fileName) throws RemoteException; 

    //Méthode qui récupère la liste de l'ensemble des clients
    List<String> getAllClients() throws RemoteException; 

    //Méthode qui récupère la taille d'un fichier
    long getFileSize(String fileName) throws RemoteException;

    void unregistredClient(String clientsToRemove) throws RemoteException;

}
