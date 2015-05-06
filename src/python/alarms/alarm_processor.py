#! /usr/bin/python

#  As a work of the United States Government, this package 
#  is in the public domain within the United States. Additionally, 
#  We waive copyright and related rights in the work worldwide
#  through the CC0 1.0 Universal Public Domain Dedication 
#  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).


__author__ = "L2EDDMAN"
__date__ = "$May 6, 2015 7:23:42 AM$"

import socket,sys
import logging
import simplejson

log = logging.getLogger("alarm_processor")
log.setLevel( logging.INFO )

def parseArgs( args ):
    import optparse
    parser=optparse.OptionParser()
    parser.add_option('-l', '--log', dest='log', default="alarms.log", help="log file", metavar="LOGFILE")
    parser.add_option('-c', '--config', dest='config', default=None, help="config file", metavar="CONFIGFILE" )
    parser.add_option('-d', '--debug', dest='debug', default=False, help="debug on", metavar="DEBUG");
    parser.add_option('-s', '--socket', dest='socket_file', default="/tmp/alarms.txt", metavar="SOCKETFILE");
    return parser.parse_args()[0]

if __name__ == "__main__":
    options = parseArgs( sys.argv );
    
    door = options.socket
    config = options.config
    
    
    # setup log
    fh = logging.handlers.TimedRotatingFileHandler(options.log,when='midnight',backupCount=7)
    if debug != False:
        fh.setLevel( logging.DEBUG )
    else:
        fh.setLevel( logging.INFO )
    fh.setFormatter( logging.Formatter( '%(asctime)s - %(name)s - %(levelname)s - %(message)s') )
    log.addHandler( fh )
    
    
    
    
    
    print "Hello World";
