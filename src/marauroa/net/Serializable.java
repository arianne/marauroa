package marauroa.net;

/** Interface of all the object that wants to be able to be converted into a stream
 *  of bytes. */
public interface Serializable
  {
  /** Method to convert the object into a stream */
  void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException;
  /** Method to build the object from a stream of bytes */
  void readObject(marauroa.net.InputSerializer in) throws java.io.IOException,
    java.lang.ClassNotFoundException;  	  
  };
