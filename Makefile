# Définition des variables utilisées dans le Makefile

USER = mbt7893
PROJECT = ~/Annee_2/Hagimule/
DWNLD_FOLDER = FichierTest  # Dossier où les fichiers téléchargés sont stockés

# Définition des commandes pour la compilation et l'exécution
JAVA = javac  # Commande pour compiler les fichiers Java
RUN = java  # Commande pour exécuter les fichiers Java

# Liste des fichiers sources à compiler
SOURCES = src/*.java  # Tous les fichiers .java dans le répertoire src et ses sous-répertoires

# Répertoire de destination pour les fichiers binaires (fichiers .class)
BIN = bin

# Liste des clients dans le réseau
CLIENTS1 = carapuce
CLIENTS2 = carapuce hippogriffe
CLIENTS3 = yoda hippogriffe manticore
CLIENTS4 = carapuce hippogriffe manticore troll
CLIENTS5 = carapuce hippogriffe manticore troll gobelin
CLIENTS6 = carapuce hippogriffe manticore troll gobelin pixie
CLIENTS7 = carapuce hippogriffe manticore troll gobelin pixie nymphe
CLIENTS8 = carapuce hippogriffe manticore troll gobelin pixie nymphe phenix
CLIENTS9 = carapuce hippogriffe manticore troll gobelin pixie nymphe phenix demusset
CLIENTS10 = carapuce hippogriffe manticore troll gobelin pixie nymphe phenix demusset basilic
# Client par défaut
CLIENTS ?= $(CLIENTS3)

# Nom du serveur
SERVER = arryn

# Client courant (peut être modifié lors de l'exécution)
CURRENT ?= arryn

# Dossier de stockage des fichiers téléchargés
STORAGE_DL ?= FichierTL/

# Latence à appliquer aux communications réseau (en ms)
LATENCY ?= 100

# Taille des paquets de données envoyés (en octets)
PACKET_SIZE ?= 1024

# Nom du fichier à télécharger
FILENAME ?= HarryPotter.mp4
# Définition des cibles
.PHONY: all clean server client close deploy downloader download_all  # Cibles qui ne sont pas des fichiers

# Cible principale : Compilation de tous les fichiers sources Java
all:
	$(JAVA) -d $(BIN) $(SOURCES)  -sourcepath src $(SOURCES)  
# Compile tous les fichiers .java dans $(SOURCES) et place les fichiers .class dans $(BIN)

# Cible pour nettoyer le projet (supprimer les fichiers compilés et téléchargés)


# Cible pour fermer toutes les fenêtres de terminal ouvertes avec 'xterm'
close:
	pkill xterm  
# Ferme tous les processus 'xterm'

# Cible pour déployer le serveur et les clients sur des machines distantes
deploy:

# Ouvre une fenêtre xterm pour se connecter au serveur et y exécuter les commandes nécessaires
	xterm -hold -T "Diary" -e 'ssh $(USER)@$(SERVER).enseeiht.fr -t "cd $(PROJECT); make; make server"' & 
	sleep 3  
# Attendre 3 secondes avant de lancer les clients

# Ouvre une fenêtre xterm pour chaque client et exécute les commandes nécessaires pour déployer chaque client
	@for CLIENT in $(shell echo $($(CLIENTS))); \
	do \
		xterm -hold -T "$$CLIENT" -e "ssh $(USER)@$$CLIENT.enseeiht.fr -t 'cd $(PROJECT); make; make client CURRENT=$$CLIENT'" & \
		sleep 1; \
	done

# Cible pour exécuter le client (DaemonImpl)
client:
	cd $(BIN) && $(RUN) Daemon $(SERVER).enseeiht.fr $(CURRENT).enseeiht.fr 
# Exécute la classe Client.DaemonImpl avec les paramètres nécessaires

# Cible pour exécuter le serveur (DiaryImpl)
server:
	cd $(BIN) && $(RUN) DiaryImpl  
# Exécute la classe Server.DiaryImpl

# Cible pour exécuter le client DownloaderImpl
downloader:
	cd $(BIN) && $(RUN) Downloader $(FILENAME) $(PROJECT)$(STORAGE_DL) $(SERVER).enseeiht.fr
# Exécute la classe Client.DownloaderImpl avec les paramètres nécessaires

console:
	cd $(BIN) && $(RUN) Console


