package org.spike;

import java.io.File;
import java.io.IOException;

public class SpikeServer extends NanoHTTPD {

	public SpikeServer(int pPort, File pWwwroot) throws IOException {
		super(pPort, pWwwroot);
	}

	// public static void main( String[] args ) {
	// try {
	// ServerSocket ss = new ServerSocket( 666 );
	// Socket s = ss.accept();
	// Writer out = new BufferedWriter( new OutputStreamWriter(
	// s.getOutputStream(), "UTF-8" ) );
	// out.write( "Hello from server!\r\n" );
	// out.flush();
	// s.close();
	// }
	// catch ( IOException ex ) {
	// System.err.println( ex );
	// }
	//
	// }
}
