package marauroa.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * test UnicodeSupportingInputStreamReader
 *
 * @author hendrik
 */
public class UnicodeSupportingInputStreamReaderTest {
	
	private BufferedReader getReader(String filename) throws IOException {
		URL url = this.getClass().getResource(filename);
		InputStream is = url.openStream(); 
		UnicodeSupportingInputStreamReader usr = new UnicodeSupportingInputStreamReader(is); 
		BufferedReader br = new BufferedReader(usr);
		return br;
	}

	private String readFirstLine(String filename) throws IOException {
		BufferedReader br = getReader(filename);
		String line = br.readLine();
		br.close();
		return line;
	}

	@Test
	public void testWithUTF8() throws IOException {
		String line = readFirstLine("utf8.txt");
		Assert.assertEquals("bleutailfly", line);
	}


	@Test
	public void testWithUTF8WithBOM() throws IOException {
		String line = readFirstLine("utf8bom.txt");
		Assert.assertEquals("bleutailfly", line);
	}
}
