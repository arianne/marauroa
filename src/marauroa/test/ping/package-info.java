/**
  * This is an small example of a MVC client approach for Marauroa.
  * 
  * This program test the whole framework from end to end.
  * It works in the next way:
  * 
  *  Client   				  Server
  * --------                 --------
  * Send Text  ----------->
  * 		   <-----------   Set state=speaking
  * 		   <-----------   Inc lines_spoken
  * 		   <-----------   Event text
  *                 .
  *                 .
  *                 .
  * 		   <-----------   Set state=idle
  * 
  * The client will stop after sending all the text it has to. 
  *                 
  */
