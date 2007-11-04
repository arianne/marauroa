package marauroa.common.io;

/*
http://koti.mbnet.fi/akini/java/unicodereader/

version: 1.1 / 2007-01-25
- changed BOM recognition ordering (longer boms first)

Original pseudocode   : Thomas Weidenfeller
Implementation tweaked: Aki Nieminen

http://www.unicode.org/unicode/faq/utf_bom.html
BOMs in byte length ordering:
  00 00 FE FF    = UTF-32, big-endian
  FF FE 00 00    = UTF-32, little-endian
  EF BB BF       = UTF-8,
  FE FF          = UTF-16, big-endian
  FF FE          = UTF-16, little-endian

Win2k Notepad:
  Unicode format = UTF-16LE
***/

import java.io.*;

/**
* This inputstream will recognize unicode BOM marks
* and will skip bytes if getEncoding() method is called
* before any of the read(...) methods.
*
* Usage pattern:
    String enc = "ISO-8859-1"; // or NULL to use systemdefault
    FileInputStream fis = new FileInputStream(file);
    UnicodeInputStream uin = new UnicodeInputStream(fis, enc);
    enc = uin.getEncoding(); // check and skip possible BOM bytes
    InputStreamReader in;
    if (enc == null) in = new InputStreamReader(uin);
    else in = new InputStreamReader(uin, enc);
*/
public class UnicodeSupportingInputStream extends InputStream {
    private PushbackInputStream internalIn;
    private boolean isInited = false;
    private String defaultEnc;
    private String encoding;
    private static final int BOM_SIZE = 4;

    /**
     * Creates a new UnicodeSupportingInputStream
     *
     * @param in  inputstream to be read
     */
    public UnicodeSupportingInputStream(InputStream in) {
        internalIn = new PushbackInputStream(in, BOM_SIZE);
    }

    /**
     * Creates a new UnicodeSupportingInputStream
     *
     * @param in  inputstream to be read
     * @param defaultEnc default encoding if stream does not have 
     *                   BOM marker. Give NULL to use system-level default.
     */
    public UnicodeSupportingInputStream(InputStream in, String defaultEnc) {
        internalIn = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEnc = defaultEnc;
    }

    public String getDefaultEncoding() {
        return defaultEnc;
    }

    public String getEncoding() {
        if (!isInited) {
            try {
                init();
            } catch (IOException ex) {
                IllegalStateException ise = new IllegalStateException("Init method failed.");
                ise.initCause(ise);
                throw ise;
            }
        }
        return encoding;
    }

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are
     * unread back to the stream, only BOM bytes are skipped.
     */
    protected void init() throws IOException {
        if (isInited) return;

        byte bom[] = new byte[BOM_SIZE];
        int n, unread;
        n = internalIn.read(bom, 0, bom.length);

        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEnc;
            unread = n;
        }

        if (unread > 0) internalIn.unread(bom, (n - unread), unread);

        isInited = true;
    }

    @Override
    public void close() throws IOException {
        isInited = true;
        internalIn.close();
    }

    @Override
    public int read() throws IOException {
        isInited = true;
        return internalIn.read();
    }
}

