Marauroa &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[![Travis](https://img.shields.io/travis/arianne/marauroa.svg)](https://travis-ci.org/arianne/marauroa) &nbsp;[![Codacy Badge](https://img.shields.io/codacy/c4048896aea4460ca6b866133c5f2f6a.svg)](https://www.codacy.com/app/arianne/marauroa) &nbsp;[![SourceForge](https://img.shields.io/sourceforge/dt/arianne.svg)](https://arianne-project.org/download/marauroa.zip) &nbsp;[![License GPL](https://img.shields.io/badge/license-GPL-blue.svg)](https://github.com/arianne/marauroa/blob/master/LICENSE.txt) 
--------
Marauroa is Arianne's Multiplayer Online Engine, that you can use to build
your own online games. Marauroa handles client-server communication and 
object persistence in a database.


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
[https://arianne-project.org](https://arianne-project.org)


Games based on Marauroa
-----------------------

Games and tools based on Marauroa:

[![Marboard](https://arianne-project.org/screens/marboard/THM_marboard_dot.png)](https://arianne-project.org/tool/marboard.html)
[![jMaPacman](https://arianne-project.org/screens/jmapacman/THM_20050702_jmapacman.jpg)](https://arianne-project.org/game/jmapacman.html)
[![Stendhal](https://arianne-project.org/screens/stendhal/THM_Stendhal98.jpg)](https://arianne-project.org/game/stendhal.html)


Developing with Marauroa
-------------------------
Please check out the tutorial on the [Marauroa Wiki](https://stendhalgame.org/wiki/Marauroa).


Building Marauroa
-----------------
To compile Marauroa you will need:

- [Java SDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Ant](https://ant.apache.org)

Now just write: `ant dist`

You need to grab a game package in order to be able to play it.



Bugs
----
Please report bugs to [https://sourceforge.net/p/arianne/bugs/](https://sourceforge.net/p/arianne/bugs/)


Testing
-------

Run `ant test` to execute the test suite.


Legal
-----
Marauroa(c) is copyright of Miguel Angel Blanch Lardin, 2003-2007,
arianne_rpg at users dot sourceforge dot net

Marauroa(c) is copyright of the Arianne Project, 2006-2016,
arianne-general at lists dot sourceforge dot net
