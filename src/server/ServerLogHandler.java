package server;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import plm.core.model.LogHandler;
/**
 * This is a dummy {@link LogHandler}, intended to not process any output from the PLM {@link plm.core.model.Game Game} engine.
 * @see LogHandler
 */
public class ServerLogHandler extends LogHandler {

	PrintStream outStream;
	public ServerLogHandler() {
		outStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
	}
	@Override
	public void log(int type, String message) {
		String level = "";
		switch(type) {
			case -1 : level = "[T]"; break;
			case 0 : 
			case 1 : level = "[D]"; break;
			case 2 : level = "[W]"; break;
			default : level = "[E]"; break;
		}
		try {
			outStream.write((" " + level + " " + message + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(int type, String message) {
		log(type, message);
	}

}
