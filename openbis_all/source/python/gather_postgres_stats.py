#!/usr/bin/python

'''
  Copyright 2012 ETH Zuerich, CISD
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

'''

# @author  Manuel Kohler

import subprocess
import shlex
import getpass
import xmlrpclib
import ConfigParser
import os
from datetime import datetime


SERVERS = {
            "openbis@openbis-phosphonetx.ethz.ch" : "phosphonetx",
            "openbis@obis.ethz.ch" : "yeastx",
            "openbis@lmc-openbis.ethz.ch" : "lmc",
            "openbis@infectx01.ethz.ch" : "infectx",
            "sbsuser@openbis-dsu.ethz.ch" : "dsu",
            "openbis@bs-plasmids.ethz.ch" : "csb",
            "openbis@openbis-scu.ethz.ch" : "scu",
            "openbis@basysbio.ethz.ch" : "basybio",
            "openbis@openbis-cina.ethz.ch" : "cina",
            "openbis@limb.ethz.ch" : "limb",
            "openbis@newchipdb.ethz.ch" : "newchipdb",
            "openbis@basynthec.ethz.ch" : "basynthec",
            "openbis@sprint-openbis.ethz.ch" : "sprint",
            "openbis@cisd-openbis.ethz.ch" : "demo",
            "openbis@cmng-openbis.dbm.unibas.ch" : "sinergia"
           }

PSQL_PROPERTIES = ["archive_mode",
                   "bgwriter_delay",
                   "bgwriter_lru_maxpages",
                   "bgwriter_lru_multiplier",
                   "checkpoint_segments",
                   "checkpoint_timeout",
                   "effective_cache_size",
                   "effective_io_concurrency",
                   "maintenance_work_mem",
                   "max_connections",
                   "server_version",
                   "shared_buffers",
                   "temp_buffers",
                   "wal_segment_size",
                  ]

SSH_BIN = "/usr/bin/ssh"
SSH_KEY = "id_rsa"
SSH_PARAMETERS = "-o StrictHostKeyChecking=no -i"
PSQL_BIN = "/usr/bin/psql"
PSQL_PARAMETERS = " -Upostgres -c \\\"show all;\\\""

SINGLE_SPACE = " "
DOUBLE_QUOTE = "\""
NEWLINE = "\n"
PIPE = "|"

SERVER = 'https://wiki-bsse.ethz.ch:8443/rpc/xmlrpc'
CONFIGFILE = "wikiPostgres.conf"

ConfluenceToken = None
ConfluenceServer = xmlrpclib.ServerProxy(SERVER)

def getAbsolutePath():
  '''
  Returns the absolute path of the script itself
  '''
  return os.path.dirname(os.path.abspath( __file__ ))

def parseConfigurationFile():
  '''
  Python 2.7 Style
  reads out the settings defined in the config file
  '''
  config = ConfigParser.ConfigParser()
  # get the absolute path to the script
  config.read(os.path.join(getAbsolutePath(), CONFIGFILE))
  config.read(CONFIGFILE)
  return config

def getTimestamp():
   return datetime.now().strftime("%d %b %Y %H:%M:%S")
   
def logIntoConfluence(config):
  '''
  Logs user into Confluence and returns a session token
  '''
  user = config.get("DEFAULT", "username")
  password = config.get("DEFAULT", "password")
  ConfluenceToken = ConfluenceServer.confluence2.login(user, password)
  return ConfluenceToken
  
def logOutConluence(token):
  '''
  Logs out from confluence and invalidates the session token
  '''
  ConfluenceServer.confluence2.logout(token)
  
def writeToConfluence (serverMap, token, config):
  '''
  Builds the content of the page and writes it into confluence 
  '''
  wikiMarkup = ""
  
  spacekey = config.get("DEFAULT", "spacekey")
  pagetitle = config.get("DEFAULT", "pagetitle")
  
  page = ConfluenceServer.confluence2.getPage(token, spacekey, pagetitle)
  if page is None:
      exit("Could not find page " + spacekey + ":" + pagetitle)
  
  wikiMarkup = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \
               \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \
               <html xmlns=\"http://www.w3.org/1999/xhtml\"> \
               <head> <title></title> </head>\
               <body> \
                 <table> \
                   <tbody>\
                     <tr> \
                       <th></th>"
   
  for server in (serverMap.iterkeys()):
    wikiMarkup += "<th>" + SERVERS[server] + "</th>"
  wikiMarkup +=  "</tr>"
   
  try:
    for property in PSQL_PROPERTIES:
      wikiMarkup +=  "<tr> <td>" + property + "</td>"
      for server in (serverMap.iterkeys()):
         wikiMarkup += "<td>" + serverMap[server][property] + "</td>"
      wikiMarkup += "</tr>"
  except:
    print("Not run correctly! Check ssh logins.")  
  
  wikiMarkup += "</tbody> </table>"
  wikiMarkup += NEWLINE + "Last update:" + SINGLE_SPACE + getTimestamp() + NEWLINE
 
  page["content"] = wikiMarkup
  ConfluenceServer.confluence2.storePage(token, page)
  
def callCommandLine(args):
  '''
  Calls a command line program and returns the standard out
  '''
  SplitArgs = shlex.split(args)
  p = subprocess.Popen(SplitArgs, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
  out, err = p.communicate()
  return out

def dictOutput (commandLineOutput):
  '''
  Parses the psql output and puts it into a dictionary which is the return value 
  '''
  # skip the header  
  settingsList = commandLineOutput.split("\n")[2:]
  settingsDict = {}
  for item in settingsList:
    try:
      splittedItem = item.split("|")[:-1]
      key, value = splittedItem[0].strip() , splittedItem[1].strip()
      settingsDict[key] = value
    except IndexError:
      pass
    except AttributeError:
      settingsDict[key] = [value]
  return settingsDict

def  main():
  serverMap = {}
  
  config = parseConfigurationFile()
  confluenceToken = logIntoConfluence(config)
  
  for (server, alias) in SERVERS.items():
    args = SSH_BIN + SINGLE_SPACE + SSH_PARAMETERS + SINGLE_SPACE + os.path.join(getAbsolutePath(), SSH_KEY) + \
      SINGLE_SPACE + server + SINGLE_SPACE + DOUBLE_QUOTE + PSQL_BIN + PSQL_PARAMETERS + DOUBLE_QUOTE
    commandLineOutput = callCommandLine(args)
    sDict = dictOutput (commandLineOutput)
    serverMap [server] = sDict
  
  writeToConfluence(serverMap, confluenceToken, config)
  logOutConluence(confluenceToken)
    
if __name__ == "__main__":
  main()
