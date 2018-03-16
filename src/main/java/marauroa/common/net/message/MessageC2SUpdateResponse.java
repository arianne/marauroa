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
import java.util.Map;

import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * Update response
 */
public class MessageC2SUpdateResponse extends Message
{

  private String response;

  /**
   * Constructor for allowing creation of an empty message
   */
  public MessageC2SUpdateResponse()
  {
    super(MessageType.C2S_UPDATE_RESPONSE, null);
  }

  /**
   * creates a new message
   *
   * @param source socket channel
   * @param response response
   */
  public MessageC2SUpdateResponse(Channel source, String response)
  {
    super(MessageType.C2S_UPDATE_RESPONSE, source);
    this.response = response;
  }

  /**
   * gets the response
   *
   * @return response
   */
  public String getResponse()
  {
    return response;
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
    out.write65536LongString(response);
  }

  @Override
  public void readObject(InputSerializer in) throws IOException
  {
    super.readObject(in);
    response = in.read65536LongString();

    if (type != MessageType.C2S_UPDATE_RESPONSE)
    {
      throw new IOException();
    }
  }

  @Override
  public void readFromMap(Map<String, Object> in) throws IOException
  {
    super.readFromMap(in);
    Object temp = in.get("response");
    if (temp != null)
    {
      response = temp.toString();
    }

    if (type != MessageType.C2S_UPDATE_RESPONSE)
    {
      throw new IOException();
    }
  }
}
