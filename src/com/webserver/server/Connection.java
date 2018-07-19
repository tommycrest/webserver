package com.webserver.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.webserver.server.HttpRequest.HttpMethod;
import com.webserver.server.HttpResponse.StatusCode;

/**
 * Connection classe che gestisce le connessioni al server implementa la interfaccia Runnable
 * 
 * by Crestanello Tommy
 * */
public class Connection implements Runnable {
	
	/**
	 * Server
	 * 
	 * */
	private Server server;
	
	/**
	 * Client
	 * 
	 * */
	private Socket client;
	
	/**
	 * Flusso di InputStream
	 * */
	private InputStream in;
	
	/**
	 * Flusso di OutputStream
	 * 
	 * */
	private OutputStream out;

	/**
	 * Costruttore della connessione al server
	 * 
	 * */
	public Connection(Socket client, Server server) {
		this.client = client;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			in = client.getInputStream();
			out = client.getOutputStream();

			// Parse della request come Http 
			HttpRequest request = HttpRequest.parseAsHttp(in);
			
			//Caso della request non null
			if (request != null) {
				//Print nell'output come viene processata la richiesta 
				System.out.println("Request per la risorsa " + request.getUrl() + " viene processata " +
					"dal socket " + client.getInetAddress() +":"+ client.getPort());
				
				//Gestiamo la response verso il client
				HttpResponse response;
				
				String method;
				
				//Sel request eseguita è GET o HEAD
				if ((method = request.getMethod()).equals(HttpMethod.GET) 
						|| method.equals(HttpMethod.HEAD)) {
					
					//Se il metodo e' GET prepariamo il file richiesto dalla url
					File f = new File(server.getWebRoot() + request.getUrl());
					response = new HttpResponse(StatusCode.OK).withFile(f);
					
					//Se il metodo è HEAD
					if (method.equals(HttpMethod.HEAD)) {
						//Rimuovo il body
						response.removeBody();
					}
				} else {
					response = new HttpResponse(StatusCode.NOT_IMPLEMENTED);
				}
				
				//Eseguo la risposta al client con la risorsa
				respond(response);
				
				//Monitoring degli header di request e di response per attivita' di debug
				System.out.println(request.getProtocol()+ " Protocollo http della request ");
				System.out.println();
				System.out.println(response.getProtocol()+" Protocollo http della response");
				System.out.println();
				System.out.println(request.toString()+ " Header della request al web server ");
				System.out.println();
				System.out.println(response.toString()+" Header della request al web server");
				//Monitoring degli header di request e di response per attivita' di debug				
				
			} else {
				
				//Gestiamo l'errore con un messaggio se avviene una richiesta NON http
				System.err.println("Server accetta richieste solo HTTP ");
			}
			
			//Chiudo gli stream 
			in.close();
			out.close();
			
		} catch (IOException e) {
			System.err.println("ERROR: IO del Client");
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				System.err.println("Errore durante la chiusura del client socket");
			}
		}
	}

	/**
	 * respond Metodo di utilita per il PrintWriter della response verso il client richiedente
	 * 
	 * @param response
	 *  
	 * */
	public void respond(HttpResponse response) {
		String toSend = response.toString();
		PrintWriter writer = new PrintWriter(out);
		writer.write(toSend);
		
		//Faccio un flush del PrintWriter
		writer.flush();
	}

}
