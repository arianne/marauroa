from marauroa.game.python import *

class TemplateGamePythonRP(PythonRP):
    def __init__(self, zone):
        self._zone=zone

    def execute(self, id, action):
        pass

    def nextTurn(self):
        pass

    def onInit(self, object):
        pass

    def onExit(self, objectid):
        pass
    
    def onTimeout(self, objectid):
        pass

    def serializeMap(self):
        pass

class TemplateZonePythonRP(PythonZoneRP):
    def __init__(self, zone):
        self._zone=zone

    def onInit():
        pass

    def onFinish():
        pass

class TemplateAIPythonRP(PythonAIRP):
    def __init__(self, rpManager):
        self._rpManager=rpManager

    def onCompute(timelimit):
        pass
    
    
    
        
