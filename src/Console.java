import java.io.File;
import java.util.Scanner;

public class Console {
    
    private static final String EXEC_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/"; // Dossier où se trouve le fichier Makefile
    private static final String DOWNLOAD_DIRECTORY = "/home/mbt7893/Annee_2/Hagimule/FichierTest"; // Dossier où se trouvent les fichiers

    public static void main(String[] args) {
        try { 
            Console console = new Console();
            console.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String nbMachines(int nbMachinesChoisies){
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
        return nbMachines;
    } 

    public void run() throws Exception {
        Scanner scanner = new Scanner(System.in);

        // Demander à l'utilisateur combien de machines il veut utiliser (au début)
        System.out.println("Veuillez choisir un nombre de machines afin de réaliser le téléchargement (entre 1 et 10) :");
        int nbMachinesChoisies = scanner.nextInt();
        String nbMachinesRec = nbMachines(nbMachinesChoisies);
        executeDeploy(nbMachinesRec);

        boolean continueDownload = true; // Variable pour gérer la boucle de téléchargement

        while (continueDownload) {

            // Demander à l'utilisateur de choisir un fichier
            System.out.print("Voulez-vous changer le nombre de machines sélectionnées ? (o/n) ");

            String choixMachine = scanner.next();

            if (choixMachine.equalsIgnoreCase("o")) {
                try {
                    String commandDeploy = "make -s close";
                    ProcessBuilder pbProcessClose = new ProcessBuilder("bash", "-c", "cd " + EXEC_DIRECTORY + " && " + commandDeploy);
                    pbProcessClose.inheritIO(); // Affiche aussi la sortie de cette commande dans le terminal
                    Process processClose = pbProcessClose.start();
                    int exitCodeClose = processClose.waitFor();
        
                    if (exitCodeClose == 0) {
                        System.out.println();
                        System.out.println("Le diary et les Daemons on été fermés.");
                    } else {
                        System.out.println();
                        System.out.println("Erreur lors de la fermeture du diary et des daemons.");
                    }
                    System.out.println("Veuillez entrer le nombre de machines : (entre 1 et 10)");
                    nbMachinesChoisies = scanner.nextInt();
                    nbMachinesRec = nbMachines(nbMachinesChoisies);
                    executeDeploy(nbMachinesRec);
                    
                } catch (Exception e) {
                    System.out.print(" ");
                    System.err.println("Erreur lors de la fermeture de " + e.getMessage());
                    e.printStackTrace();
                }
                
            }

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
                    System.out.print("Voulez-vous télécharger un autre fichier ? (o/n) : ");
                    String response = scanner.next();

                    if (response.equalsIgnoreCase("n")) {
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
                                System.out.println("Le diary et les Daemons on été fermés.");
                            } else {
                                System.out.println();
                                System.out.println("Erreur lors de la fermeture du diary et des daemons.");
                            }
                        } catch (Exception e) {
                            System.out.print(" ");
                            System.err.println("Erreur lors de la fermeture de " + e.getMessage());
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

    public void executeDeploy(String clientGroup) throws Exception {
        try {
            System.out.print("Le diary et les Daemons se lancent...\n");
            // Construire la commande pour déployer avec le groupe de clients choisi
            String commandDeploy = "make -s deploy CLIENTS=" + clientGroup;
            ProcessBuilder pbDeploy = new ProcessBuilder("bash", "-c", "cd " + EXEC_DIRECTORY + " && " + commandDeploy);
            pbDeploy.inheritIO(); // Afficher la sortie dans le terminal
            Process processDeploy = pbDeploy.start();
            int exitCodeDeploy = processDeploy.waitFor();

            if (exitCodeDeploy != 0) {
                System.out.println("Lancement du diary ou des daemons a échoué.");
            } 
            
            // Vérifier si le DiaryImpl est lancé sur le serveur
            Process processDiaryCheck = new ProcessBuilder("ssh", "user@server", "pgrep", "-f", "DiaryImpl").start();
            int diaryExitCode = processDiaryCheck.waitFor();
            System.out.println(diaryExitCode);
            // Vérifier si les Daemons sont lancés sur les clients
            Process processDaemonCheck = new ProcessBuilder("ssh", "user@client", "pgrep", "-f", "DaemonImpl").start();
            int daemonExitCode = processDaemonCheck.waitFor();
            if (daemonExitCode != 255) {
                throw new Exception("Un ou plusieurs Daemons n'ont pas démarré correctement.");
            }
        } catch (Exception e) {
            
            try {
                String commandDeploy = "make -s close";
                ProcessBuilder pbProcessClose = new ProcessBuilder("bash", "-c", "cd " + EXEC_DIRECTORY + " && " + commandDeploy);
                pbProcessClose.inheritIO(); // Affiche aussi la sortie de cette commande dans le terminal
                Process processClose = pbProcessClose.start();
                int exitCodeClose = processClose.waitFor();
    
                if (exitCodeClose != 0) {
                    System.out.println();
                    throw new Exception("Erreur lors de la fermeture du diary et des daemons.");
                }
            } catch (Exception ex) {
                System.out.print(" ");
                System.err.println(ex.getMessage());
                e.printStackTrace();
            }

            throw new Exception("Erreur lors du déploiement: " + e.getMessage());

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
            } else {
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