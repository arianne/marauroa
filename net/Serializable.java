package marauroa.net;

public interface Serializable
  {
  void writeObject(marauroa.net.OutputSerializer out) throws java.io.IOException;
  void readObject(marauroa.net.InputSerializer in) throws java.io.IOException,
    java.lang.ClassNotFoundException;  	  
  };
