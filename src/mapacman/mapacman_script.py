from marauroa.game import *
from marauroa.net import *
from marauroa import marauroad
from mapacman import *

from java.io import ByteArrayOutputStream

pacman_mapfile='map_definition.txt'

#
# PythonRP Interface for Java classes
# * We need it because calling functions is VERY slow
#

class RealPythonRP(PythonRP):
    __map=None
    __removed_elements=[]
    __super_players=[]
    __online_players=[]
    __zone=None
    
    def __init__(self,zone):
        self.__zone=zone
        self.__map=mapacmanRPMap(self,pacman_mapfile)
        
    def getZone(self):
        return self.__zone

    def execute(self, id, action):
        """ called to execute actions from player identified by id that wants
        to do action action """
      
        result=0
        player=self.__zone.get(id)
        action_code=action.get("type")

        if action_code=="turn":
            result=self.turn(player,action.get("dir"))
        elif action_code=="chat":
            result=self.chat(player,action.get("content"))
        else:
            print "action not registered"
        
        print "Player doing ",
        print action.toString(),
        print " with result ",
        print result
    
        return result
        
    def move(self, player):
        """ This methods try to move the player and return the new position """
        x=player.getInt("x")
        y=player.getInt("y")
        dir=player.get("dir")
    
        if dir=='N' and (y-1)>=0 and self.__map.get(x,y-1)<>'*':
            y=y-1
            player.put("y",y)
        elif dir=='W' and (x-1)>=0 and self.__map.get(x-1,y)<>'*':
            x=x-1
            player.put("x",x)
        elif dir=='S' and (y+1)<self.__map.sizey() and self.__map.get(x,y+1)<>'*':
            y=y+1
            player.put("y",y)
        elif dir=='E' and (x+1)<self.__map.sizex() and self.__map.get(x+1,y)<>'*':
            x=x+1
            player.put("x",x)
            
        return (x,y)

    def canMove(self, player, dir):
        """ This methods try to move the player and return the new position """
        x=player.getInt("x")
        y=player.getInt("y")
    
        if dir=='N' and (y-1)>=0 and self.__map.get(x,y-1)<>'*':
            return 1
        elif dir=='W' and (x-1)>=0 and self.__map.get(x-1,y)<>'*':
            return 1
        elif dir=='S' and (y+1)<self.__map.sizey() and self.__map.get(x,y+1)<>'*':
            return 1
        elif dir=='E' and (x+1)<self.__map.sizex() and self.__map.get(x+1,y)<>'*':
            return 1
        else:
            return 0
    
    def turn(self, player, direction):
        result=failed
        if _directions.count(direction)==1 and self.canMove(player,direction):
            player.put("dir",direction)
            self.__zone.modify(player)
            result=success
        return result

    def chat(self, player, content):
        player.put("?text",content)
        self.__zone.modify(player)
        return success

    def __removeBall(self, ball, pos):
        self.__zone.remove(RPObject.ID(ball))
        self.__map.removeZoneRPObject(pos)
        element={'timeout':ball.getInt("!respawn"),'object':ball}
        self.__removed_elements.append(element)
    
    def __addBall(self, ball):
        self.__map.addZoneRPObject(ball['timeout'])
        self.__removed_elements.remove(ball)
    
    def __movePlayer(self,player):
        print 'You move in %s direction' % player.get("dir")
        pos=self.move(player)
        if self.__map.hasZoneRPObject(pos):
            object_in_pos=self.__map.getZoneRPObject(pos)
            if object_in_pos.get("type")=="ball":
                self.__removeBall(object_in_pos,pos)
                    
                # Increment the score of the player
                player.put("score",player.getInt("score")+1)
                self.__zone.modify(player)
            elif object_in_pos.get("type")=="superball":
                self.__removeBall(object_in_pos,pos)
                    
                # Notify to remove the attribute on timeout
                timeout=object_in_pos.getInt("!timeout")
                player.put("super",timeout)
                element={'timeout':timeout,'object':player}
                self.__super_players.append(element)
                self.__zone.modify(player)
        

    def __ghostCollisions(self, player):
        pos=(player.getInt("x"),player.getInt("y"))
        for player_in_pos in self.getPlayers(pos):
            if player_in_pos.get("type")=="ghost":
                if player.has("super"):
                    # Eat the ghost
                    pass
                else:
                    # kill the player
                    pass
    
    def __foreachPlayer(self):
        for player in self.__online_players:
            if(self.canMove(player,player.get("dir"))):
                self.__movePlayer(player)
                self.__ghostCollisions(player)

            else:
                print 'You CAN\'T move in %s direction' % player.get("dir")
    
    def nextTurn(self):
        """ execute actions needed to place this code on the next turn """
        for object in self.__removed_elements:
            if object['timeout']==0:
                self.__addBall(object)
            else:
                object['timeout']=object['timeout']-1

        for object in self.__super_players:
            if object['timeout']==0:
                object['object'].remove("super")
                self.__super_players.remove(object)
            else:
                object['timeout']=object['timeout']-1
                object['object'].put("super",object['timeout'])
                self.__zone.modify(object['object'])

        self.__foreachPlayer()
        

    def getPlayers(self,pos):
        list=[]
        for player in self.__online_players:
            if pos[0]==player.getInt("x") and pos[1]==player.getInt("y"):
                list.append(player)
        return list
        
    def onInit(self, object):
        """ Do what you need to initialize this player """
        pos=self.__map.getRandomRespawn()
        object.put("x",pos[0])
        object.put("y",pos[1])
        
        self.__zone.add(object)
        self.__online_players.append(object)
        return 1

    def onExit(self, objectid):
        """ Do what you need to remove this player """
        for x in self.__online_players:
            if x.getInt("id")==objectid.getObjectID():
                self.__online_players.remove(x)
                break
            
        self.__zone.remove(objectid)
        return 1

    def onTimeout(self, objectid):
        return onExit(self,objectid)
    
    def serializeMap(self):
        return self.__map.serializeMap()


    def createPlayer(self, name):
        """ This function create a player """
        object=RPObject()
        object.put("id",self.__zone.create().get("id"))
        object.put("type","player");
        object.put("name",name)
        object.put("x",0)
        object.put("y",0)
        object.put("dir",randomDirection())
        object.put("score",0)
        return object;

    def createGhost(self, name):
        """ This function create a ghost """
        object=RPObject()
        object.put("id",self.__zone.create().get("id"))
        object.put("type","ghost");
        object.put("name",name)
        object.put("x",0)
        object.put("y",0)
        object.put("dir",randomDirection())
        return object;

    def createBall(self, x,y):
        """ This function create a Ball object that when eats by player increments
        its score. """
        object=RPObject()
        object.put("id",self.__zone.create().get("id"));
        object.put("type","ball");
        object.put("x",x)
        object.put("y",y)
        object.put("!score",1)
        object.put("!respawn",60)
        return object;

    def createSuperBall(self, x,y):
        """ This function create a SuperBall object that when eats by player
        make it to be able to eat and destroy the ghosts """
        object=self.createBall(x,y)
        object.put("type","superball");
        object.put("!timeout",15)
        return object;

    
class mapacmanRPMap:
    __grid=[]
    __zone=None
    __respawnPoints=[]
    __last_respawnPoints=0
    __objects_grid={}
    
    def __init__(self, pythonRP, sizex, sizey):
        self.__grid=[]
        self.__zone=pythonRP.getZone()
        for i in range(sizex):
            grid_line=''
            for j in range(sizey):
                grid_line=grid_line+' '
            self.__grid.append(grid_line)
    
    def __init__(self, pythonRP, filename):
        f=open(filename,'r')
        line=f.readline()
        
        self.__zone=pythonRP.getZone()
        
        i=0
        while line<>'':
            j=0
            for char in line[:-1]:
                if char=='.':
                    self.addZoneRPObject(pythonRP.createBall(j,i))
                elif char=='0':
                    self.addZoneRPObject(pythonRP.createSuperBall(j,i))
                j=j+1
                
            self.__grid.append(line[:-1])
            self.computeRespawnPoints(i,line[:-1])
            line=f.readline()
            i=i+1
    
    def computeRespawnPoints(self,y,line):
        i=line.find('+')
        while i<>-1:
            pos=(i,y)
            self.__respawnPoints.append(pos)
            i=line.find('+',i+1)
    
    def get(self,x,y):
        return (self.__grid[y])[x]
    
    def hasZoneRPObject(self, pos):
        return self.__objects_grid.has_key(pos)
    
    def getZoneRPObject(self,pos):
        return self.__objects_grid[pos]
    
    def addZoneRPObject(self,object):
        x=object.getInt("x")
        y=object.getInt("y")
        
        self.__objects_grid[(x,y)]=object
        self.__zone.add(object)
    
    def removeZoneRPObject(self,pos):
        del self.__objects_grid[pos]

    def sizey(self):
        return len(self.__grid)
    
    def sizex(self):
        return len(self.__grid[0])
    
    def getRandomRespawn(self):
        self.__last_respawnPoints=(self.__last_respawnPoints+1)%(len(self.__respawnPoints))
        return self.__respawnPoints[self.__last_respawnPoints]
    
    def serializeMap(self):
        def createBlock(pos):
            object=RPObject()
            object.put("x",pos[0])
            object.put("y",pos[1])
            object.put("type","block")
            return object
            
        list=java.util.LinkedList()
        for j in grid:
            for i in grid[j]:
                if (grid[j])[i]=='*':
                    list.add(createBlock((i,j)))

        return list
        
#
# A few constants to make things more beautiful
#
success=1
failed=0

_directions=['N','W','S','E']

def randomDirection():
    return directions[int((rand()/32768)*4)]

#
#  RPObject python creation methods.
#
 


#
# Test the whole thing
#
    
if __name__=='__main__':
    """ Test case here """
    pass
    
