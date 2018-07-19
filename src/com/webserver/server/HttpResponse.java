package com.webserver.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * HttpResponse classe che mappa e forma le response del server
 * 
 * by Crestanello Tommy
 * 
 * */
public class HttpResponse {
	
	/**
	 * protocollo HTTP/1.0: con questo campo posso disambiguare al server
	 * la modalita' con cui viene gestito il protocollo di response
	 * 
	 * Default se la richiesta viene eseguita HTTP/1.1 il server risponde con HTTP/1.0
	 * 
	 * Variando la variabile "protocol" di questa classe, possiamo far gestire 
	 * una HTTP/1.1 al server e rispondere Keep-Alive.
	 * 
	 * */
	private static final String protocol = "HTTP/1.0";

	/**
	 * Status della response
	 * 
	 * */
	private String status;
	
	/**
	 * mappa degli header delle response http
	 * 
	 * */
	private NavigableMap<String, String> headers = new TreeMap<String, String>();
	
	/**
	 * body della responde come array di byte
	 * 
	 * */
	private byte[] body = null;

	/**
	 * costruttore con il solo parametro di status
	 * 
	 * */
	public HttpResponse(String status) {
		this.status = status;
		setDate(new Date());
	}

	/**
	 * response del server con file. Il server è file-based
	 * 
	 * */
	public HttpResponse withFile(File f) {
		if (f.isFile()) {
			try {
				FileInputStream reader = new FileInputStream(f);
				int length = reader.available();
				body = new byte[length];
				reader.read(body);
				reader.close();
				
				setContentLength(length);
				//controllo se il file in responso è htm / html o text
				if (f.getName().endsWith(".htm") || f.getName().endsWith(".html")) {
					//setto il content type
					setContentType(ContentType.HTML);
				} else {
					//setto il content type
					setContentType(ContentType.TEXT);
				}
			} catch (IOException e) {
				System.err.println("Errore in fase di lettura risorsa " + f);
			}
			return this;
		} else {
			//caso in cui la request e' eseguita su una risorsa non presente sul server
			return new HttpResponse(StatusCode.NOT_FOUND)
				.withHtmlBody("<html><body>File " + f + " not found.</body></html>");
		}
	}

	/**
	 * costruiamo la response con un body di tipo html
	 * 
	 * */
	public HttpResponse withHtmlBody(String msg) {
		setContentLength(msg.getBytes().length);
		setContentType(ContentType.HTML);
		body = msg.getBytes();
		return this;
	}

	/**
	 * metodo di utilita' per inserire la data all'interno degli header della response
	 * 
	 * */
	public void setDate(Date date) {
		headers.put("Date", date.toString());
	}

	/**
	 * metodo di utlita' per inserire il content lenght all'interno degli header della response
	 * 
	 * */
	public void setContentLength(long value) {
		headers.put("Content-Length", String.valueOf(value));
	}

	/**
	 * metodo di utlita' per inserire il content type all'interno degli header della response
	 * 
	 * */
	public void setContentType(String value) {
		headers.put("Content-Type", value);
	}

	/**
	 * metodo di utilita' per la rimozione del body nella response
	 * 
	 * */
	public void removeBody() {
		body = null;
	}

	@Override
	public String toString() {
		String result = protocol + " " + status +"\n";
		for (String key : headers.descendingKeySet()) {
			result += key + ": " + headers.get(key) + "\n";
		}
		result += "\r\n";
		if (body != null) {
			result += new String(body);
		}
		return result;
	}

	/**
	 * Inner class per gli status code delle risorse erogate dal web server
	 * 
	 * */
	public static class StatusCode {
		public static final String OK = "200 OK";
		public static final String NOT_FOUND = "404 Not Found";
		public static final String NOT_IMPLEMENTED = "501 Not Implemented";
	}

	/**
	 * Inner class per la classificazione del content type delle response del web server
	 * Il server eroga risorse solamente con content text/plain e text/html
	 * 
	 * */
	public static class ContentType {
		public static final String TEXT = "text/plain";
		public static final String HTML = "text/html";
	}
	
	public String getProtocol() {
		return protocol;
	}

}
