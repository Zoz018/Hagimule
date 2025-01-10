import java.io.File;
import java.util.Scanner;

public class Console {
    
    private static final String EXEC_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/"; // Dossier où se trouve le fichier Makefile
    private static final String DOWNLOAD_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/FichierTest"; // Dossier où se trouvent les fichiers

    public static void main(String[] args) {
        Console console = new Console();
        console.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        // Demander à l'utilisateur de choisir un groupe de clients
        System.out.println("Veuillez choisir un nombre de machines afin de réaliser le téléchargement (entre 1 et 10):");

        int nbMachinesChoisies = scanner.nextInt();
        String nbMachines = "";

        switch (nbMachinesChoisies) {
            case 1:
                nbMachines = "CLIENTS1";
                break;
            case 2:
                nbMachines = "CLIENTS2";
                break;
            case 3:
                nbMachines = "CLIENTS3";
                break;
            case 4:
                nbMachines = "CLIENTS4";
                break;
            case 5:
                nbMachines = "CLIENTS5";
                break;
            case 6:
                nbMachines = "CLIENTS6";
                break;
            case 7:
                nbMachines = "CLIENTS7";
                break;
            case 8:
                nbMachines = "CLIENTS8";
                break;
            case 9:
                nbMachines = "CLIENTS9";
                break;
            case 10:
                nbMachines = "CLIENTS10";
                break;
            default:
                System.out.println("Choix invalide. Utilisation par défaut d'une seule machine.");
                nbMachines = "CLIENTS1";
        }

        

        executeDeploy(nbMachines);

        boolean continueDownload = true; // Variable pour gérer la boucle de téléchargement

        while (continueDownload) {
            // Affiche les fichiers disponibles dans le répertoire
            System.out.println();
            System.out.println("Voici les fichiers disponibles pour le téléchargement :");

            File folder = new File(DOWNLOAD_DIRECTORY);
            File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                int i = 1;
                for (File file : files) {
                    if (file.isFile()) {
                        System.out.println(i + ". " + file.getName());
                        i++;
                    }
                }

                // Demander à l'utilisateur de choisir un fichier
                System.out.print("Entrez le numéro du fichier à télécharger : ");
                int choice = scanner.nextInt();

                if (choice > 0 && choice <= files.length) {
                    // Récupère le fichier sélectionné
                    File selectedFile = files[choice - 1];
                    String fileName = selectedFile.getName();
                    System.out.println("Vous avez choisi : " + fileName);
                    System.out.println();
                    System.out.print("------------TELECHARGEMENT------------");
                    System.out.println();

                    // Appelle la méthode pour télécharger le fichier
                    startDownload(fileName);

                    // Demander à l'utilisateur s'il veut télécharger un autre fichier
                    System.out.println();
                    System.out.print("Voulez-vous télécharger un autre fichier ? (oui/non) : ");
                    String response = scanner.next();

                    if (response.equalsIgnoreCase("non")) {
                        continueDownload = false; // Arrêter la boucle si l'utilisateur répond "non"
                        System.out.println("Exit ...");

                        try {
                            String commandDeploy = "make -s close";
                                ProcessBuilder pbProcessClose = new ProcessBuilder("bash", "-c", "cd " + EXEC_DIRECTORY + " && " + commandDeploy);
                                pbProcessClose.inheritIO(); // Affiche aussi la sortie de cette commande dans le terminal
                                Process processClose = pbProcessClose.start();
                                int exitCodeClose = processClose.waitFor();
                
                                if (exitCodeClose == 0) {
                                    System.out.println();
                                    System.out.print("Le diary et les Daemons on été fermés.");
                                } else {
                                    System.out.println();
                                    System.out.println("Erreur lors de la fermeture du diary et des daemons.");
                                }
                        } catch (Exception e) {
                            System.out.print(" ");
                            System.err.println("Erreur lors du lancement de " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Choix invalide. Veuillez réessayer.");
                }
            } else {
                System.out.println("Aucun fichier trouvé dans le dossier.");
                continueDownload = false; // Si aucun fichier n'est trouvé, on sort de la boucle
            }
        }

        scanner.close(); // Ferme le scanner une fois la boucle terminée
    
    }

    public void executeDeploy(String clientGroup) {
        try {
            System.out.print("Le diary et les Daemons se lancent...\n");
            // Construire la commande pour déployer avec le groupe de clients choisi
            String commandDeploy = "make -s deploy CLIENTS=" + clientGroup;
            ProcessBuilder pbDeploy = new ProcessBuilder("bash", "-c", "cd " + EXEC_DIRECTORY + " && " + commandDeploy);
            pbDeploy.inheritIO(); // Afficher la sortie dans le terminal
            Process processDeploy = pbDeploy.start();
            int exitCodeDeploy = processDeploy.waitFor();

            if (exitCodeDeploy == 0) {
                System.out.println("Le diary et les Daemons tournent.");
            } else {
                System.out.println("Lancement du diary ou des daemons a échoué.\n");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement de " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startDownload(String fileName) {
        // Commande à exécuter pour télécharger le fichier
        try {

            String commandDownloader = "make -s downloader FILENAME=" + fileName;
            ProcessBuilder pbDownloader = new ProcessBuilder("bash", "-c", "cd " + EXEC_DIRECTORY + " && " + commandDownloader);
            pbDownloader.inheritIO(); // Affiche aussi la sortie de cette commande dans le terminal
            Process processDownloader = pbDownloader.start();
            int exitCodeDownloader = processDownloader.waitFor();

            if (exitCodeDownloader == 0) {
                System.out.print("--------------------------------------");
                System.out.println();
                System.out.println("Téléchargement du fichier " + fileName + " terminé.");
            } else {
                System.out.print("--------------------------------------");
                System.out.println();
                System.out.println("Erreur lors du téléchargement du fichier.");
            }

        } catch (Exception e) {
            System.out.print(" ");
            System.err.println("Erreur lors du démarrage du téléchargement : " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }
}