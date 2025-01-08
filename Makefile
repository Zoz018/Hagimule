# Définition des variables utilisées dans le Makefile

USER = mbt7893  # Nom d'utilisateur pour la connexion SSH
PROJECT = ~/Documents/Annee_2/Hagimule/  # Chemin du projet sur la machine distante
DWNLD_FOLDER = FichierTest  # Dossier où les fichiers téléchargés sont stockés

# Définition des commandes pour la compilation et l'exécution
JAVA = javac  # Commande pour compiler les fichiers Java
RUN = java  # Commande pour exécuter les fichiers Java

# Liste des fichiers sources à compiler
SOURCES = src/*.java  # Tous les fichiers .java dans le répertoire src et ses sous-répertoires

# Répertoire de destination pour les fichiers binaires (fichiers .class)
BIN = bin  

# Liste des clients dans le réseau
CLIENTS = bobafett chewie dagobah ewok lando kenobi palpatine luke  

# Nom du serveur
SERVER = ackbar  

# Liste des ports utilisés pour la communication
PORTS = 8081:8082:8083:8084  

# Client courant (peut être modifié lors de l'exécution)
CURRENT ?= ackbar  

# Dossier de stockage des fichiers téléchargés
STORAGE_DL ?= FichierTest/  

# Latence à appliquer aux communications réseau (en ms)
LATENCY ?= 10  

# Taille des paquets de données envoyés (en octets)
PACKET_SIZE ?= 1024  

# Définition des cibles
.PHONY: all clean server client close deploy downloader tests  # Cibles qui ne sont pas des fichiers

# Cible principale : Compilation de tous les fichiers sources Java
all:
	$(JAVA) -d $(BIN) $(SOURCES)  -sourcepath src $(SOURCES)  
# Compile tous les fichiers .java dans $(SOURCES) et place les fichiers .class dans $(BIN)

# Cible pour nettoyer le projet (supprimer les fichiers compilés et téléchargés)
#clean:
#	rm -rf $(BIN)/Client/*  
# Supprime les fichiers .class pour les clients dans $(BIN)
#	rm -rf $(BIN)/Server/*  
# Supprime les fichiers .class pour le serveur dans $(BIN)
#	rm -rf $(DWNLD_FOLDER)/*  
# Supprime les fichiers téléchargés dans $(DWNLD_FOLDER)

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
	@for CLIENT in $(CLIENTS); \
	do \
		xterm -hold -T "$$CLIENT" -e "ssh $(USER)@$$CLIENT.enseeiht.fr -t 'cd $(PROJECT); make; make client CURRENT=$$CLIENT'" & \
		sleep 1; \
	done

# Cible pour exécuter le client (DaemonImpl)
client:
	cd $(BIN) && $(RUN) Daemon $(CURRENT).enseeiht.fr $(PORTS) ../Storage/ $(SERVER).enseeiht.fr $(PACKET_SIZE) $(LATENCY)  
# Exécute la classe Client.DaemonImpl avec les paramètres nécessaires

# Cible pour exécuter le serveur (DiaryImpl)
server:
	cd $(BIN) && $(RUN) DiaryImpl  
# Exécute la classe Server.DiaryImpl

# Cible pour exécuter le client DownloaderImpl
downloader:
	cd $(BIN) && $(RUN) Downloader $(SERVER) $(STORAGE_DL)  
# Exécute la classe Client.DownloaderImpl avec les paramètres nécessaires

# Cible pour effectuer des tests en téléchargeant plusieurs fois depuis le serveur
tests:
	@cd $(BIN) && \
	for i in $(shell seq 1 10); \  
# Exécute 10 fois la commande suivante
	do \
		$(RUN) Client.DownloaderTest $(SERVER) $(STORAGE_DL) "tests.log"; \  
# Lance le test de téléchargement et enregistre les résultats dans "tests.log"
	done
