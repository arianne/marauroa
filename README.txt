-------------------------------------------------------------------------------
| This program is free software; you can redistribute it and/or modify it under
| the terms of the GNU General Public License as published by the Free Software
| Foundation.
|
| This program is distributed in the hope that it will be useful, but WITHOUT
| ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
| FOR A PARTICULAR PURPOSE.  See the file LICENSE for more details.
-------------------------------------------------------------------------------

Marauroa
--------
Marauroa is Arianne's Multiplayer Online Engine, that you can use to build
your own online games using Marauroa for object management, database persistence
and perception based client-server communication.


Extended Description
--------------------
Marauroa is completely written in Java using a multithreaded server architecture
with a TCP oriented network protocol, a SQL based persistence engine and a
flexible game system based on open systems totally expandible and modifiable
by developers. 

Marauroa is based on a philosophy we call Action/Perception, on each turn a
perception is sent to clients explaining them what they perceive and clients
can ask server to do any action in their names using actions.
Marauroa is totally game agnostic and makes very little assumptions about what
are you trying to do, allowing a great freedom to create whatever type of game
you want.

You can find the latest version of Marauroa at:
http://arianne.sourceforge.net

Marauroa is a GPL project.


Requirements
------------
To actually compile Marauroa you will need:
- Java SDK 1.5 http://java.sun.com
- Ant 1.6 http://ant.apache.org

In order to get it working with MySQL you will also need:
- MySQL Connector/J http://www.mysql.com/downloads/api-jdbc-stable.html

- JUnit 4.4 in order to run test cases.
- Jython for Python support.


Build
-----
Make sure ant ( http://ant.apache.org/ ) is installed.
Now just write:

  ant jar

Or for a complete package release write:

  ant



You need to grab a game package in order to be able to play it.


Running
-------
Ok, you have compiled Marauroa. Now what to do?

Well, if you have built a Marauroa server it is because you
want to play with the server either because you want to run your own
game server or because you are a developer wanting to develop your own game.
If you don't understand the above it is likely that you are looking for
http://arianne.sourceforge.net instead.


Bugs
----
Please refer bugs to http://sourceforge.net/tracker/?group_id=1111&atid=101111


Testing
-------
In order to test Marauroa with JUnit you need to setup a MySQL database:
 
  Database: marauroatest
  username: junittest 
  password: passwd
 
You can do that by doing:

  create database marauroatest;
  grant all on marauroatest.* to junittest@localhost identified by 'passwd';

Also please copy src/marauroa/test/server.ini to main project folder.

  cp src/marauroa/test/server.ini .  
  



Legal
-----
Marauroa(c) is copyright of Miguel Angel Blanch Lardin, 2003-2007
arianne_rpg at users dot sourceforge dot net
Marauroa(c) is copyright of the Arianne Project, 2006-2011
arianne-general at lists dot sourceforge dot net
