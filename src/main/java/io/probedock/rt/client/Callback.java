package io.probedock.rt.client;

import com.github.nkzawa.emitter.Emitter;

/**
 * Empty implementation of IOCallback to use in the client
 * 
 * @author Laurent Prevost <laurent.prevost@probedock.io>
 */
public class Callback implements Emitter.Listener {
	@Override
	public void call(Object... args) {
		synchronized (this) {
			this.notify();
		}
	}
}
