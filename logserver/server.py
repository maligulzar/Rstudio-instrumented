#!/usr/bin/env python2.4
#
# Copyright 2007 Google Inc. All Rights Reserved.

import BaseHTTPServer
import SimpleHTTPServer
import urllib
import random
import sys
import datetime


class MyHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):

  def do_GET(self):
    form = {}
    now = datetime.datetime.now()
    logname =  "log/"+now.strftime("%Y%m%d") + ".txt"
    if self.path.find('?') > -1:
      queryStr = self.path.split('?')[1]
      for queryParam in queryStr.split('&'):
        if queryParam.split('=')[0] == "data":
  	  f = open(logname, 'a')
	  f.write(urllib.unquote_plus(queryParam[5:] + ",\n"))
	  f.close()





    self.wfile.flush()
    self.connection.shutdown(1)

print sys.argv[1]
bhs = BaseHTTPServer.HTTPServer(('', int(sys.argv[1])), MyHandler)
bhs.serve_forever()
