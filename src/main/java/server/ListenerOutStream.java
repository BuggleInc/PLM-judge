package main.java.server;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import main.java.server.listener.BasicListener;

public class ListenerOutStream extends FilterOutputStream {

	BasicListener link = null;
	private ListenerOutStream(OutputStream o) {
		super(o);
	}
	
	public ListenerOutStream(OutputStream o, BasicListener b) {
		this(o);
		link = b;
	}
	
	public void resetListener(BasicListener b) {
		link = b;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
        link.streamOut(new String(b, off, len));
	}
}
