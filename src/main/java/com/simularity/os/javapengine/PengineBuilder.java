package com.simularity.os.javapengine;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import com.simularity.os.javapengine.exception.CouldNotCreateException;
import com.simularity.os.javapengine.exception.PengineNotReadyException;

/*
 * Copyright (c) 2016 Simularity Inc.
 * 

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 * 
 */

/**
 * A builder for Pengine
 * 
 * 
 * Pengine Life cycle:
 * 
 * <ol>
 * <li>instantiate a PengineBuilder</li>
 * <li>set various properties on it</li>
 * <li>call create on it to make Pengines, respecting the slave limit</li>
 * <li>Use and destroy the pengines</li>
 * </ol>
 * 
 * If you have destroy set to true in PengineBuilder, the Pengine will be destroyed automatically at the end of the query.
 * 
 * @author Anne Ogborn
 *
 */
public final class PengineBuilder implements Cloneable {
	private URL server = null;
	private String application = "sandbox";
	private String ask = null;
	private int chunk = 1;
	private boolean destroy = true;
	private String srctext = null;
	private URL srcurl = null;
	private final String format = "json";
	private String alias = null;
	
	
	/**
	 * Create a new PengineBuilder
	 */
	public PengineBuilder() {
		super();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final synchronized PengineBuilder clone() throws CloneNotSupportedException {
		return (PengineBuilder)super.clone();
	}

	/**
	 * Get the actual URL to request from
	 * 
	 * 
	 * @param action the action to request - create, send, etc - as a string. Note this is the URI endpoint name, not the pengine action
	 * @return the URL to perform the request to
	 * 
	 * @throws PengineNotReadyException 
	 */
	synchronized URL getActualURL(String action) throws PengineNotReadyException {
		StringBuffer msg = new StringBuffer("none");
		
		if(server == null) {
			throw new PengineNotReadyException("Cannot get actual URL without setting server");
		}
		try {		
			URI uribase = server.toURI();
			if (uribase.isOpaque()) {
				throw new PengineNotReadyException("Cannot get actual URL without setting server");
			}
			
			URI relative = new URI("/pengine/" + action);
			
			URI fulluri = uribase.resolve(relative);
			msg.append(fulluri.toString());
			return fulluri.toURL();
		} catch (MalformedURLException e) {
			throw new PengineNotReadyException("Cannot form actual URL for action " + action + " from uri " + msg.toString());
		} catch (URISyntaxException e) {
			throw new PengineNotReadyException("URISyntaxException in getActualURL");
		}
	}
	
	/**
	 * Get the actual URL to request from
	 * 
	 * @param action the action to request - create, send, etc - as a string. Note this is the URI endpoint name, not the pengine action
	 * @param id the pengine ID
	 * @return the created URL
	 * 
	 * @throws PengineNotReadyException 
	 */
	synchronized URL getActualURL(String action, String id) throws PengineNotReadyException  {
		StringBuffer msg = new StringBuffer("none");
		
		if(server == null) {
			throw new PengineNotReadyException("Cannot get actual URL without setting server");
		}
		try {		
			URI uribase = server.toURI();
			if (uribase.isOpaque()) {
				throw new PengineNotReadyException("Cannot get actual URL without setting server");
			}
			
			URI relative;
			try {
				relative = new URI("/pengine/" + action + "?format=json&id=" + URLEncoder.encode(id, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// stupid checked exception
				e.printStackTrace();
				return null;
			}
			
			URI fulluri = uribase.resolve(relative);
			
			msg.append(fulluri.toString());
			return fulluri.toURL();
		} catch (MalformedURLException e) {
			throw new PengineNotReadyException("Cannot form actual URL for action " + action + " from uri " + msg.toString());
		} catch (URISyntaxException e) {
			throw new PengineNotReadyException("URISyntaxException in getActualURL");
		}
	}

	/**
	 * @return a string representation of the request body for the create action
	 */
	synchronized String getRequestBodyCreate() {
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder job = factory.createObjectBuilder();
		
		if(!this.destroy) {
			job.add("destroy", "false");
		}
		if(this.chunk > 1) {
			job.add("chunk", this.chunk);
		}
		job.add("format", this.format);

		if(this.srctext != null) {
			job.add("srctext", this.srctext);
		}
		if(this.srcurl != null) {
			job.add("srcurl", this.srcurl.toString());
		}
		
		if(this.ask != null) {
			job.add("ask", this.ask);
		}
		
		return job.build().toString();
	}

	/**
	 * @param urlstring String that represents the server URL - this does not contain the /pengines/create extension
	 * @throws MalformedURLException if the string can't be turned into an URL
	 */
	synchronized public void setServer(String urlstring) throws MalformedURLException {
		server = new URL(urlstring);
	}

	/**
	 * Set the server URL. Usually this is just the domain, eg. http://pengines.swi-prolog.org/
	 * 
	 * @param server the server base URL - this does not contain the /pengines/create extension
	 */
	synchronized public void setServer(URL server) {
		this.server = server;
	}
	
	/**
	 * @return the server
	 */
	public URL getServer() {
		return server;
	}


	/**
	 * A pengine server can have different applications with different exposed API's
	 * 
	 * @return the application name
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	synchronized public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * @return the query that will be sent along with the create, or null if none
	 */
	public String getAsk() {
		return ask;
	}

	/**
	 * @param ask the query to be sent along with the create, or null to not send one
	 */
	synchronized public void setAsk(String ask) {
		this.ask = ask;
	}

	/**
	 * @return the number of answers to return in one HTTP request
	 */
	public int getChunk() {
		return chunk;
	}

	/**
	 * @param chunk the max number of answers to return in one HTTP request - defaults to 1
	 */
	synchronized public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	/**
	 * @return true if we will destroy the pengine at the close of the first query
	 */
	public boolean isDestroy() {
		return destroy;
	}

	/**
	 * @param destroy Destroy the pengine when the first query concludes?
	 */
	synchronized public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	/**
	 * @return the srctext  @see setSrctext
	 */
	public String getSrctext() {
		return srctext;
	}

	/**
	 * @param srctext Additional Prolog code, which must be safe, to be included in the pengine's knowledgebase
	 */
	synchronized public void setSrctext(String srctext) {
		this.srctext = srctext;
	}

	/**
	 * @return the URL of some additional Prolog code, which must be safe, to be included in the pengine's knowledgebase
	 */
	public URL getSrcurl() {
		return srcurl;
	}

	/**
	 * @param srcurl the srcurl to set
	 */
	synchronized public void setSrcurl(URL srcurl) {
		this.srcurl = srcurl;
	}

	/**
	 * @return the alias or null
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias a string name to refer to the pengine by (remove by passing this null)
	 */
	synchronized public void setAlias(String alias) {
		this.alias = alias;
	}

	synchronized public Pengine newPengine() throws CouldNotCreateException {
		return new Pengine(this);
	}

	/**
	 * return the POST body for a /pengines/ask request of ask
	 * 
	 * @param id   The pengine id that is transmitting
	 * @param ask   The Prolog query
	 * @return   the body
	 */
	public String getRequestBodyAsk(String id, String ask) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("ask(");
		sb.append(ask);
		sb.append(",[]).");     // TODO template, chunk go here
		return sb.toString();
	}

	/**
	 * @return the POST body for next operation
	 */
	public String getRequestBodyNext() {
		return "next.";
	}

	/**
	 * @return the POST body for destroy operation
	 */
	public String getRequestBodyDestroy() {
		return "destroy.";
	}

	/**
	 * dump some debug information
	 */
	public void dumpDebugState() {
		System.err.println("--- PengineBuilder ----");
		System.err.println("alias " + this.alias);
		System.err.println("application " + this.application);
		System.err.println("ask " + this.ask);
		System.err.println("chunk size " + Integer.toString(this.chunk));
		if(this.destroy)
			System.err.println("destroy at end of query");
		else
			System.err.println("retain at end of query");
		
		System.err.println("server " + this.server);
		System.err.println("srctext " + this.srctext);
		System.err.println("srcurl " + this.srcurl);
		System.err.println("--- end PengineBuilder ---");
	}

	/**
	 * @return the POST body for stop operation
	 */
	public String getRequestBodyStop() {
		return "stop.";
	}

	/**
	 * @return the POST body for pull_response
	 */
	public String getRequestBodyPullResponse() {
		return "pull_response.";
	}

	/**
	 * return true if we have an ask
	 * 
	 * @return true if we have an ask
	 */
	boolean hasAsk() {
		return this.ask != null;
	}
	
	/**
	 * remove any ask we have
	 */
	public void removeAsk() {
		this.ask = null;
	}
}
