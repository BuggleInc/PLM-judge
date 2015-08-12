package generator;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ListenerOutStream extends FilterOutputStream {

	BasicListener link = null;
	private ListenerOutStream(OutputStream o) {
		super(o);
	}
	
	public ListenerOutStream(OutputStream o, BasicListener listener) {
		this(o);
		link = listener;
	}
	
	public void resetListener(BasicListener b) {
		link = b;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
        link.streamOut(new String(b, off, len));
	}
}
