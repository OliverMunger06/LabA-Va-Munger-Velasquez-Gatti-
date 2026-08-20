CineMax - Sistema di gestione per cinema monosala
==================================================

Progetto sviluppato per il Laboratorio Interdisciplinare A.
CineMax è un'applicazione Java da terminale pensata per la gestione di un
piccolo cinema monosala da 200 posti. Il programma consente la consultazione
 del palinsesto, la registrazione dei clienti, la gestione delle prenotazioni e
le operazioni riservate a proiezionisti e bigliettai.

Autori
------
- Munger Oliver, matricola 764208, sede VA
- Velasquez Carpio Davide Noe, matricola 765163, sede VA
- Gatti Davide, matricola 765949, sede VA

Struttura del repository
------------------------
La struttura prevista del progetto è la seguente:

.
|-- autori.txt
|-- README.txt
|-- src/
|   |-- cinemax/
|       |-- CineMax.java
|       |-- Users/
|       |-- controls/
|       |-- gestione/
|       |-- utils/
|-- data/
|   |-- film.csv
|   |-- palinsesto.csv
|   |-- prenotazioni.csv
|   |-- utenti.csv
|-- doc/
|   |-- Manuale_Utente_CineMax.pdf
|   |-- Manuale_Tecnico_CineMax.pdf
|   |-- JavaDoc/
|       |-- index.html
|-- bin/
|   |-- CineMax.jar
|-- lib/

Nota: la cartella lib puo' rimanere vuota se non vengono utilizzate librerie
esterne. Il progetto è pensato per funzionare esclusivamente con Java e file
locali, senza database relazionali o servizi esterni.

Requisiti
---------
- Java Development Kit, versione recente. Si consiglia JDK 16 o superiore.
- Sistema operativo Windows, macOS o Linux.
- Terminale o prompt dei comandi.
- Facoltativo: un IDE Java come IntelliJ IDEA, Eclipse o NetBeans.

Classe principale
-----------------
La classe principale da eseguire è:

cinemax.CineMax

Il metodo main si trova nel file:

src/cinemax/CineMax.java

File dati richiesti
-------------------
Il programma legge e scrive i dati nella cartella data, che deve trovarsi nella
radice del progetto. I percorsi sono relativi alla cartella da cui viene avviata
l'applicazione.

File principali:

- data/utenti.csv
  Contiene gli utenti registrati e gli utenti predefiniti del sistema.
  I ruoli previsti sono CLIENTE, PROIEZIONISTA e BIGLIETTAIO.

- data/film.csv
  Contiene le informazioni dei film: titolo, genere, regista, anno, durata ed
  eta' minima consigliata.

- data/palinsesto.csv
  Contiene le proiezioni disponibili, con data, ora, film associato, costo del
  biglietto e posti disponibili.

- data/prenotazioni.csv
  Contiene le prenotazioni effettuate dai clienti. Il file puo' essere vuoto
  all'avvio, ma deve essere presente nella cartella data.

Attenzione: avviare il programma sempre dalla radice del progetto. Se viene
avviato da un'altra cartella, l'applicazione potrebbe non trovare i file CSV.

Compilazione da terminale
-------------------------
Aprire un terminale nella cartella radice del progetto.

Su macOS o Linux:

find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt

Su Windows, usando il Prompt dei comandi:

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt

Dopo la compilazione, i file .class vengono generati nella cartella out.

Esecuzione da terminale senza jar
---------------------------------
Dalla radice del progetto eseguire:

java -cp out cinemax.CineMax

Se il programma parte correttamente, viene mostrato il menu iniziale:

=== BENVENUTO IN CINEMAX ===
1. Registrati
2. Log In
3. Entra come Guest
4. Esci dall'applicazione

Creazione del file jar
----------------------
Dopo aver compilato il progetto, creare la cartella bin se non esiste gia'.

Su macOS o Linux:

mkdir -p bin
jar cfe bin/CineMax.jar cinemax.CineMax -C out .

Su Windows, se la cartella bin non esiste:

mkdir bin
jar cfe bin\CineMax.jar cinemax.CineMax -C out .

Esecuzione del file jar
-----------------------
Dalla radice del progetto eseguire:

java -jar bin/CineMax.jar

Nota importante: anche quando si usa il jar, la cartella data deve rimanere
nella radice del progetto, allo stesso livello delle cartelle src, bin e doc.

Esecuzione da IDE
-----------------
1. Aprire la cartella del progetto con l'IDE scelto.
2. Impostare il JDK del progetto.
3. Verificare che la cartella data sia presente nella radice del progetto.
4. Aprire il file src/cinemax/CineMax.java.
5. Eseguire il metodo main della classe cinemax.CineMax.

Funzionalita' principali
-----------------------
Utente guest:
- ricerca delle proiezioni;
- visualizzazione dei dettagli delle proiezioni;
- accesso alla registrazione come cliente.

Cliente registrato:
- creazione di una prenotazione;
- visualizzazione delle proprie prenotazioni;
- modifica di una prenotazione;
- cancellazione di una prenotazione;
- logout.

Proiezionista:
- inserimento di nuove proiezioni;
- modifica della data e dell'orario di una proiezione;
- eliminazione di una proiezione;
- logout.

Bigliettaio:
- visualizzazione delle prenotazioni della data odierna;
- ricerca di una prenotazione;
- visualizzazione del dettaglio di una prenotazione;
- logout.

Credenziali e utenti di test
----------------------------
Il file data/utenti.csv contiene gia' alcuni utenti di test con ruolo
PROIEZIONISTA e BIGLIETTAIO, come richiesto dalle specifiche del progetto.

Le password non sono salvate in chiaro: nel file utenti.csv viene memorizzato
l'hash della password. Per aggiungere nuovi clienti è possibile usare la voce
"Registrati" dal menu principale.

Documentazione
--------------
La documentazione del progetto va collocata nella cartella doc.

File consigliati:

- doc/Manuale_Utente_CineMax.pdf
  Manuale destinato all'utente finale, con istruzioni di installazione,
  avvio e uso delle funzionalita'.

- doc/Manuale_Tecnico_CineMax.pdf
  Manuale destinato a un lettore tecnico, con descrizione dell'architettura,
  delle classi principali, dei file dati e delle scelte implementative.

- doc/JavaDoc/index.html
  Pagina principale della documentazione JavaDoc generata dal codice sorgente.

Generazione della JavaDoc
-------------------------
Dalla radice del progetto è possibile generare la JavaDoc con il comando:

javadoc -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -d doc/JavaDoc -sourcepath src -subpackages cinemax

Il file principale da aprire è:

doc/JavaDoc/index.html

Pulizia dei file generati
-------------------------
I file e le cartelle generati durante compilazione e test possono essere
rimossi senza perdere il codice sorgente:

- out/
- sources.txt
- eventuali file temporanei creati durante la modifica dei CSV

Non eliminare la cartella data, perchè contiene i file necessari al
funzionamento dell'applicazione.

Note operative
--------------
- Il programma è una TUI, cioè un'applicazione con interfaccia testuale da
  terminale.
- Non è prevista una GUI.
- Non è previsto l'uso di database relazionali.
- Non è necessario gestire accessi concorrenti o architetture client/server.
- I dati vengono mantenuti tramite file CSV locali.
- In caso di problemi di lettura o scrittura, verificare che i file CSV siano
  presenti e che il programma sia avviato dalla radice del progetto.

