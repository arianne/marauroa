/** *************************************************************************
 *                   (C) Copyright 2003-2017 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ************************************************************************** */
package marauroa.common.net.message;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * Transfers an update
 */
public class MessageS2CUpdate extends Message
{
  private static Logger logger = Logger.getLogger(MessageS2CUpdate.class);

  private byte[] update;

  /**
   * Constructor for allowing creation of an empty message
   */
  public MessageS2CUpdate()
  {
    super(MessageType.S2C_UPDATE, null);
  }

  /**
   * creates a new message
   *
   * @param source socket channel
   * @param update update
   */
  public MessageS2CUpdate(Channel source, byte[] update)
  {
    super(MessageType.S2C_UPDATE, source);
    this.update = update.clone();
  }

  /**
   * gets the update
   *
   * @return update
   */
  public byte[] getUpdate()
  {
    if (update == null)
    {
      return update;
    }
    return update.clone();
  }

  @Override
  public String toString()
  {
    return "Message (S2C Update)";
  }

  @Override
  public void writeObject(OutputSerializer out) throws IOException
  {
    super.writeObject(out);
    out.write(update);
  }

  @Override
  public void writeToJson(StringBuilder out)
  {
    super.writeToJson(out);
    out.append(",\"update\":");
    try
    {
      OutputSerializer.writeJson(out, new String(update, "UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      logger.error(e, e);
    }
  }

  @Override
  public void readObject(InputSerializer in) throws IOException
  {
    super.readObject(in);
    update = in.readByteArray();

    if (type != MessageType.S2C_UPDATE)
    {
      throw new IOException();
    }
  }
}
