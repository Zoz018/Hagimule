import java.rmi.*;
import java.util.List;

//Interface RMI de l'annuaire 
public interface DiaryInterface extends Remote {
    //Méthode qui permet d'enregistrer les fichiers des cients dans l'annuaire
    public void registerFile(String clientID, String fileName) throws RemoteException; 

    //Méthode la liste des client qui possède le fichier
    List<String> getClient(String fileName ) throws RemoteException; 
}
