package marauroa.client.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Normal persistence using files
 *
 * @author hendrik
 */
public class FileSystemPersistence extends Persistence {
	private String homedir = System.getProperty("user.home") + "/"; 

	/**
	 * creates a "normal" FileSystemPersistence
	 */
	FileSystemPersistence() {
		// package visibile only
	}
	
	private String concatFilename(boolean relativeToHome, String basedir, String filename) {
		String file = basedir + "/" + filename;
		if (relativeToHome) {
			file = homedir + file;
		}
		return file;
	}

	@Override
	public InputStream getInputStream(boolean relativeToHome, String basedir, String filename) throws IOException {
		return new FileInputStream(concatFilename(relativeToHome, basedir, filename));
	}

	@Override
	public OutputStream getOutputStream(boolean relativeToHome, String basedir, String filename) throws IOException {
		return new FileOutputStream(concatFilename(relativeToHome, basedir, filename));
	}

}
