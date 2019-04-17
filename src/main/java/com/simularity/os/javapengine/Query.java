/**
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
package com.simularity.os.javapengine;

import java.util.Iterator;
import java.util.Vector;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.simularity.os.javapengine.exception.PengineNotReadyException;

/**
 * @author Anne Ogborn
 *
 * Representation of a pengine query in progress or dead
 * 
 *  To get one, make a pengine, then use the pengine to make a query.
 *  
 */
public class Query implements Iterator<Proof> {

	private boolean hasMore = true;  // there are more answers on the server
    private boolean succeeded = false; // A solution has yet to be delivered!
	private Pengine p;
	private Vector<JsonObject> availProofs = new Vector<JsonObject>();
	
	/**
	 * @param pengine the pengine that is making the query
	 * @param ask the Prolog query as a string
	 * @param queryMaster if true, set off the process to make the query on the Pengine slave.
	 * 
	 * @throws PengineNotReadyException if the pengine's got a query already or is destroyed
	 */
	Query(Pengine pengine, String ask, boolean queryMaster) throws PengineNotReadyException {
		p = pengine;
		
		if(queryMaster) {
			p.doAsk(this, ask);
		}
	}

	/**
	 * return the next proof, or null if not available
	 * 
	 * Note that it is theoretically impossible to always know that there are more
	 * proofs available. Caller always needs to be ready to handle a null
	 * 
	 * It is guaranteed that if you get a null from this the query is done and will
	 * never return a non-null in the future.
	 * 
	 * Note that we don't throw the PengineNotReadyException. This is to conform to the Iterator interface
	 * 
	 * @return  the next proof, or null if not available
	 */
	synchronized public Proof next() {
        // we may have consumed non-data messages before now.
        while (!succeeded) {
            try {
                // Either we get the result, or consume more output events
                p.doPullResponse();
            } catch (PengineNotReadyException e) {
                e.printStackTrace();
                return null;
            }
        }
		// the was data available
		if(!availProofs.isEmpty()) {
			JsonObject data = availProofs.get(0);
			availProofs.remove(0);
			if(!hasMore && availProofs.isEmpty())
				p.iAmFinished(this);
			
			return new Proof(data);
		}
		
		// we don't have any available and the server's done
		if(!hasMore) {
			return null;
		}
		
        succeeded=false;

		// try to get more from the server
		try {
			p.doNext(this);
		} catch (PengineNotReadyException e) {
            e.printStackTrace();
            return null;  // we do this to conform to the Iterator interface
        }
		
		// if we now have data, we have to do just like above
		if(!availProofs.isEmpty()) {
			JsonObject data = availProofs.get(0);
			availProofs.remove(0);
			if(!hasMore && availProofs.isEmpty())
				p.iAmFinished(this);
			return new Proof(data);
		} else {  // we asked for data and didn't get it, the server must be done
			if(hasMore)System.err.println("Why is hasMore true here?");
			
			return null;
		}
	}
	
	/**
	 * signal the query that there are no more Proofs of the query available.
	 * message sent from the http world
	 * 
	 */
	synchronized void noMore() {
		if(!hasMore)  // must never call iAmFinished more than once
			return;
		
		hasMore = false;
		if(availProofs.isEmpty())
			p.iAmFinished(this);
		
		// we might be held externally, waiting to deliver last Proof or no-more-Proof result
	}

	/**
	 * Callback from the http world that we've got new data from the slave
	 * 
	 * @param newDataPoints
	 */
	synchronized void addNewData(JsonArray newDataPoints) {
		for(Iterator<JsonValue> iter = newDataPoints.listIterator(); iter.hasNext() ; availProofs.add( ((JsonObject)iter.next())));
	}
	
	/**
	 * 
	 * @return true if we <b>think</b> we have more data. 
	 */
	public boolean hasNext() {
		return hasMore || !availProofs.isEmpty();
	}

	/**
	 * dump some debug information
	 */
	public void dumpDebugState() {
		if(this.hasMore)
			System.err.println("has more solutions");
		else
			System.err.println("no more solutions");
		
		System.err.println("availproofs" + this.availProofs.toString());
		System.err.println("pengine is " + this.p.getID());
	}

	/**
	 * Stop the query on the slave. We're done.
	 * We might make a query that we only need the first few lines of, for example.
	 * 
	 * Equivalent of typing period at the top-level in Prolog
	 * 
	 * @throws PengineNotReadyException 
	 * 
	 */
	public void stop() throws PengineNotReadyException {
        if (hasMore)
            p.doStop();
		
		hasMore = false;
		availProofs.clear();
		
		p.iAmFinished(this);
	}

    public void succeeded() {
        succeeded = true;
    }
}
