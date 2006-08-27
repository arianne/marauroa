package marauroa.common.io;

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

	/**
	 * create the filename string
	 *
	 * @param relativeToHome should this file be placed below the users home directory?
	 * @param basedir directory prefix which is ignore in webstart environment
	 * @param filename filename (without path)
	 * @return filename
	 */
	private String concatFilename(boolean relativeToHome, String basedir, String filename) {
		StringBuilder file = new StringBuilder();
		if (relativeToHome) {
			file.append(homedir);
		}
		if ((basedir != null) && (!basedir.trim().equals(""))) {
			file.append(basedir);
			file.append("/");
		}
		file.append(filename);
		return file.toString();
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
