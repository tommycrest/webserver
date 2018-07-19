package com.webserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Server
 * 
 * by Crestanello Tommy
 * 
 */
public class Server implements Runnable {

	/**
	 * Porta di ascolto del server
	 * 
	 */
	private int SERVER_PORT = 8080;

	/**
	 * Root Folder
	 * 
	 */
	private String SERVER_WEB_ROOT = "www";

	/**
	 * ServerSocket
	 * 
	 */
	private ServerSocket socket;

	/**
	 * Limite massimo di thread
	 * 
	 */
	private int MAX_THREADS = 10;

	/**
	 * WebServerTheradPool
	 * 
	 */
	private ExecutorService webServerThreadPool;

	/**
	 * run() method override da interfaccia Runnable
	 */
	@Override
	public void run() {
		try

		{
			socket = new ServerSocket(SERVER_PORT);
			webServerThreadPool = Executors.newFixedThreadPool(MAX_THREADS);
		} catch (IOException e) {
			System.err.println("ERROR: Server cannot listen on port " + SERVER_PORT);
			System.exit(1);
		}
		
		//Messaggio di avvenuto startup del server con PORTA, WEB_ROOT e MAX_THREAD
		System.out.println("Running server on port " + SERVER_PORT + " web folder located \"" + SERVER_WEB_ROOT
				+ "\" and " + MAX_THREADS + " as threads limit.");

		// Ciclo while per i Thread del Server
		while (!Thread.interrupted()) {
			try {
				webServerThreadPool.execute(new Thread(new Connection(socket.accept(), this)));
			} catch (IOException e) {
				System.err.println("Cannot accept client.");
			}
		}

		close();

	}

	/**
	 * close() : metodo di chiusura del server e del Thread Pool. 
	 * Chiude tutto il pool di threads gestiti dal server gestendo una IOException in casi di anomalia.
	 * 
	 * */
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("ERROR: in closing server socket.");
		}
		webServerThreadPool.shutdown();
		try {
			if (!webServerThreadPool.awaitTermination(10, TimeUnit.SECONDS))
				webServerThreadPool.shutdownNow();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * getWebRoot() metodo di utilità per avere la web root definita dove abbiamo le risorse statiche
	 * da erogare.
	 * */
	public String getWebRoot() {
		return SERVER_WEB_ROOT;
	}

	/**
	 * Costruttore della classe con i parametri base:
	 * 
	 * @param SERVER_PORT
	 * @param SERVER_WEB_ROOT
	 * @param MAX_THREAD
	 * 
	 * */
	public Server(final int serverPort, final String webRoot, final int maxThreads) {
		this.MAX_THREADS = maxThreads;
		this.SERVER_WEB_ROOT = webRoot;
		this.SERVER_PORT = serverPort;
	}

	/**
	 * Main di attivazione del server
	 * 
	 * */
	public static void main(String[] args) {
		int port = 8080;
		String webRoot = "www";
		int maxThreads = 10;
		// Specifichiamo alla esecuzione del Sever eventuali parametri da line di
		// comando
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help"))
			System.out.println(
					"Use: java -cp WebServer.jar com.webserver.server.Server <port> <web root> <threads limit>\n");
		else {
			port = Integer.parseInt(args[0]);
			webRoot = args[1];
			maxThreads = Integer.parseInt(args[2]);
		}
		// Se non vengono specificati parametri all'avvio del Server, vengono presi in
		// considerazione
		// i parametri di default specificati all'interno del codice
		new Thread(new Server(port, webRoot, maxThreads)).start();
	}
}
