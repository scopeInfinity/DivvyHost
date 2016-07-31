# DivvyHost
Decentralized Hosting : Fully P2P Hosting 

[![Build Status](https://travis-ci.org/scopeInfinity/DivvyHost.svg?branch=master)](https://travis-ci.org/scopeInfinity/DivvyHost)


The Project aims for decentralized Hosting instead of a normal static IP or nameserver based Web Hosting.

Any User can use DivvyHost to upload his HTML website and only he can modify it later on.

Divvy Client Spreaded Over Intranet having a intranet IP, finds other DivvyClient shares website content which it contains with other, leading to spread of latest data all over intranet. Those HTML website uploaded from any user availabe to everyone even if few of client is down/off/terminated.


## Dependencies

  * Java 1.7 or higher
  
  * Ant 1.8.0 or higher (Compiling)

  * Python 2 (Hosting)  

## Compile
    cd /path/to/DivvyHost/
    ant compile

## Distribution
  Distribution Directory `/path/to/DivvyHost/dist`
  
    cd /path/to/DivvyHost/
    ant jar
    
## Executing
  
    cd /path/to/DivvyDistribution/
    java -jar DivvyHost.jar
    
  
  __Without GUI__
  
    cd /path/to/DivvyDistribution/
    java -jar DivvyHost.jar -nogui

  __Using Bash Script__

   Service Options are for controlling program in background
   
   Default Logs Path `/tmp/divvy.log`

     Usage: bin.sh <option> [DivvyHost.jar]
       
     Options:
      -help    Help
      -run   Run With GUI
      -nogui   Run Without GUI
      -test    Check Any Running Instance
      -show    Show GUI
      -hide    Hide GUI

    Service Options:
     start    Start DivvyHost
     stop   Stop DivvyHost
     reload   Reload DivvyHost
  
  Example
  
    cd /path/to/DivvyDistribution/
    bash bin.sh -run


  
    
  
