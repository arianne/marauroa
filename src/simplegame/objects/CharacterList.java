import marauroa.game.RPObject;
import marauroa.game.RPObjectFactory;
import marauroa.game.Attributes;
import marauroa.net.OutputSerializer;
import marauroa.net.InputSerializer;
import marauroa.marauroad;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Iterator;


public class CharacterList
 extends RPObject
{
  public final static int TYPE_CHARACTER_LIST=4;
  public final static int TYPE_CHARACTER_LIST_ENTRY=5;
  
  private LinkedList charList;

  public CharacterList()
  {
    objectType=TYPE_CHARACTER_LIST;
  }

  public void writeObject(OutputSerializer out) 
    throws IOException
  {
    try
    {
      marauroad.trace("CharacterList.writeObject()","<");
      super.writeObject(out);

      if(charList!=null)
      {
        out.write((int)charList.size());
        Iterator it=charList.iterator();
        while(it.hasNext())
        {
          RPObjectFactory.getFactory().addRPObject(out,(RPObject)it.next());
        }
      }
      else
      {
        out.write((int)0);
      }
    }
    finally
    {
      marauroad.trace("CharacterList.writeObject()",">");
    }
  }//writeObject

  public void readObject(InputSerializer in) 
    throws IOException, ClassNotFoundException
  {
    try
    {
      marauroad.trace("CharacterList.readObject()","<");
      super.readObject(in);
      int size=in.readInt();
      marauroad.trace("CharacterList.readObject()","D",size+" characters found");
      if(size>0)
      {
        charList=new LinkedList();
        for(int i=0;i<size;++i)
        {
         charList.add(RPObjectFactory.getFactory().getRPObject(in));
        }
      }
    }
    finally
    {
      marauroad.trace("CharacterList.readObject()",">");
    }
  }//readObject

  public void addCharacter(int char_id, String char_name, String char_status)
  {
    CharEntry entry = new CharEntry();
    entry.setName(char_name);
    entry.setId(char_id);
    entry.setStatus(char_status);
    if(charList==null)
    {
      charList = new LinkedList();
    }
    charList.add(entry);  
  }

  public CharEntryIterator iterator()
  {
    return(new CharEntryIterator(charList);
  }
 
  private class CharEntryIterator
  {
    private Iterator iterator;
    public CharEntryIterator(List list)
    {
      if(list!=null)
      {
        iterator = list.iterator();
      }
      else
      {
        iterator = null;
      }
    }

    public boolean hasNext()
    {
      return(iterator!=null && iterator.hasNext());
    }

    public CharEntry next()
    {
      return((CharEntry)iterator.next());
    }
  }//CharEntryIterator

  public static class CharEntry 
   extends RPObject
  {
    public CharEntry()
    {
      objectType=TYPE_CHARACTER_LIST_ENTRY;
    }
    
    public void setName(String char_name)
    {
      put("name",char_name);
    }

    public String getName()
     throws Attributes.AttributeNotFoundException
    {
      return(get("name"));
    } 
     
    public void setId(int char_id)
    {
      put("id",char_id);
    }

    public String getId()
     throws Attributes.AttributeNotFoundException
    {
      return(get("id"));
    }

    public void setStatus(String char_st)
    {
      put("st",char_st);
    }

    public String getStatus()
     throws Attributes.AttributeNotFoundException
    {
      return(get("st"));
    }


  }
}
