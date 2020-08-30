package marauroa.common.net.message;

import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * This message indicate the server to create an account with token.
 *
 * @author maxgmer
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SCreateAccountWithToken extends Message {

    /** Desired username */
    private String username;

    /** Field to send data you need about token type */
    private String tokenType;

    /** Token (usually given by a third party auth provider */
    private String token;

    /** client language */
    private String language = Locale.ENGLISH.getLanguage();

    /** Constructor for allowing creation of an empty message */
    public MessageC2SCreateAccountWithToken() {
        super(MessageType.C2S_CREATE_ACCOUNT_WITH_TOKEN, null);
    }

    /**
     * Constructor with a TCP/IP source/destination of the message and username, token
     * associated to the account to be created.
     *
     * @param source
     *            The TCP/IP address associated to this message
     * @param username
     *            desired username
     * @param tokenType
     *            token type
     * @param token
     *            authentication token
     * @param language
     *            client language
     */
    public MessageC2SCreateAccountWithToken(Channel source, String username, String tokenType,
                                            String token, String language) {
        super(MessageType.C2S_CREATE_ACCOUNT_WITH_TOKEN, source);
        this.username = username;
        this.token = token;
        this.tokenType = tokenType;
        this.language = language;
    }

    /**
     * Returns desired account's username
     * @return desired account's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns token type.
     * @return token type
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns authentication token
     * @return authentication token
     */
    public String getToken() {
        return token;
    }

    /**
     * gets the language
     *
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * This method returns a String that represent the object
     *
     * @return a string representing the object.
     */
    @Override
    public String toString() {
        return "Message (C2S CreateAccountWithToken) from (" + getAddress()
                + ") CONTENTS: (" + username + ";" + tokenType + ";" + token + ")";
    }

    @Override
    public void writeObject(OutputSerializer out) throws IOException {
        super.writeObject(out);
        out.write(username);
        out.write(tokenType);
        out.write(token);
        out.write255LongString(language);
    }

    @Override
    public void readObject(InputSerializer in) throws IOException {
        super.readObject(in);
        username = in.readString();
        tokenType = in.readString();
        token = in.readString();
        if (in.available() > 0) {
            language = in.read255LongString();
        }

        if (type != MessageType.C2S_CREATE_ACCOUNT_WITH_TOKEN) {
            throw new IOException();
        }
    }

    @Override
    public void readFromMap(Map<String, Object> in) throws IOException {
        super.readFromMap(in);
        if (in.get("u") != null) {
            username = in.get("u").toString();
        }
        if (in.get("tokenType") != null) {
            tokenType = in.get("tokenType").toString();
        }
        if (in.get("token") != null) {
            token = in.get("token").toString();
        }
        if (in.get("l") != null) {
            language = in.get("l").toString();
        }
        if (type != MessageType.C2S_CREATE_ACCOUNT_WITH_TOKEN) {
            throw new IOException();
        }
    }
}