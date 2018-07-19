package com.webserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * HttpRequest classe che mappa le richieste http al server
 * 
 * by Crestanello Tommy
 * */
public class HttpRequest {

	/**
	 * method della richiesta http
	 * 
	 * */
	private String method;
	
	/**
	 * url della richiesta http
	 * 
	 * */
	private String url;
	
	/**
	 * protocollo della richiesta
	 * 
	 * */
	private String protocol;
	
	/**
	 * mappa degli header delle richiesta http
	 * 
	 * */
	private NavigableMap<String, String> headers = new TreeMap<String, String>();
	
	/**
	 * body della richiesta http
	 * 
	 * */
	private List<String> body = new ArrayList<String>();

	/**
	 * costruttore di default
	 * 
	 * */
	private HttpRequest() {}

	/**
	 * metodo di utilita per il parsing della richiesta come http
	 * 
	 * @param in InputStream
	 * 
	 * */
	public static HttpRequest parseAsHttp(InputStream in) {
		try {
			HttpRequest request = new HttpRequest();
			
			//Creo un BufferReader dall'InpuStream
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = reader.readLine();
			
			//se la lettura e' null il server non interpreta la richiesta
			//gestendo una IOException
			if (line == null) {
				throw new IOException("Server accetta solo richieste HTTP");
			}
			
			String[] requestLine = line.split(" ", 3);
			if (requestLine.length != 3) {
				throw new IOException("Impossibile il parsing da linea \"" + line + "\"");
			}
			if (!requestLine[2].startsWith("HTTP/")) {
				throw new IOException("Server accetta solo richieste HTTP");
			}
			
			//Se la richiesta è well-formed:
			request.method = requestLine[0];
			request.url = requestLine[1];
			request.protocol = requestLine[2];
			
			line = reader.readLine();
			while(line != null && !line.equals("")) {
				String[] header = line.split(": ", 2);
				if (header.length != 2)
					//Controllo e gestisco errore sull'header
					throw new IOException("Impossibile il parsing dell'header da linea \"" + line + "\"");
				else 
					request.headers.put(header[0], header[1]);
				line = reader.readLine();
			}
			
			//Non ci sono errori nell'header posso soddisfare la request
			while(reader.ready()) {
				line = reader.readLine();
				request.body.add(line);
			}
			
			return request;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	/**
	 * getMethod metodo di utilita 
	 * 
	 * */
	public String getMethod() {
		return method;
	}
	
	/**
	 * getUrl metodo di utilita 
	 * 
	 * */
	public String getUrl() {
		return url;
	}
	
	/**
	 * getProtocol metodo di utilita 
	 * 
	 * */
	public String getProtocol() {
		return protocol;
	}
	
	@Override
	public String toString() {
		String result = method + " " + url + " " + protocol + "\n";
		for (String key : headers.keySet()) {
			result += key + ": " + headers.get(key) + "\n";
		}
		result += "\r\n";
		for (String line : body) {
			result += line + "\n"; 
		}
		return result;
	}
	
	/**
	 * Inner class per specificare i metodi accettati dal server
	 * 
	 * GET
	 * HEAD
	 * 
	 * */
	public static class HttpMethod {
		public static final String GET = "GET";
		public static final String HEAD = "HEAD";
	}
	
}
