import java.rmi.*;
import java.rmi.registry.LocateRegistry;

public class DiaryServer {
    public static void main(String[] args){
        try {
            //Initialisation de l'annuaire 
            DiaryImpl diary = new DiaryImpl();
            //Localisation ou création de l'annuaire sur le port 8080 et on enregistre l'objet diary sous le nom "Annuaire"
            LocateRegistry.createRegistry(8080).rebind("Annuaire", diary);
            //Debug 
            System.out.println("Le serveur de l'annuaire tourne...");
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
    }
}
