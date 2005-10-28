package marauroa.common.game;

import java.util.*;

class RPSlotDesc implements marauroa.common.net.Serializable
  {
  private static short lastCode=0;
  private static Map<String,Short> slotIntegerMap=new HashMap<String,Short>();

  private static short getValidCode(String name)
    {
    if(!slotIntegerMap.containsKey(name))
      {
      slotIntegerMap.put(name,new Short(++lastCode));
      }

    return (slotIntegerMap.get(name)).shortValue();
    }

  public RPSlotDesc()
    {
    }

  public RPSlotDesc(String name, byte capacity, byte flags)
    {
    code=getValidCode(name);
    this.name=name;
    this.capacity=capacity;
    this.flags=flags;
    }

  public short code;
  public String name;
  public byte capacity;
  public byte flags;

  public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException
    {
    out.write(code);
    out.write(name);
    out.write(capacity);
    out.write(flags);
    }

  public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException
    {
    code=in.readShort();
    name=in.readString();
    capacity=in.readByte();
    flags=in.readByte();
    }
  }

