#! /usr/bin/env python

# Client and server for udp (datagram) echo.
#
# Usage: udpecho -s [port]            (to start a server)
# or:    udpecho -c host [port] <file (client)

import sys
from socket import *

import requests

BUFSIZE = 1024
PORT = 7070


def main():
    server()


def server():
    s = socket(AF_INET, SOCK_DGRAM)
    s.bind(('', PORT))
    print('udp echo server ready')
    while 1:
        data, addr = s.recvfrom(BUFSIZE)
        print('server received %r from %r' % (data, addr))
        url = "http://localhost:8080/api/avatarService/v1/device/update/mpack"
        try:
            requests.post(url=url, data=data, headers={'Content-Type': 'application/octet-stream'})
            print("posted data")
        except:
            print("post error")


main()
