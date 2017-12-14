import SocketServer
import re
from DesktopEnvironment import DesktopEnvironment

class TrackpadServer():
    HOST = ''
    PORT = 50047
    def __init__(self):
        self.server = SocketServer.TCPServer((self.HOST, self.PORT), TcpHandler)
    def connect(self):
        self.server.serve_forever(0.1)
        self.server.close()

class TcpHandler(SocketServer.StreamRequestHandler):

    desktop = DesktopEnvironment()
    def handle(self):
        self.data = self.request.recv(1024).strip()
        self.data = re.sub('^\W*', '', self.data)
        print '{} wrote:{}'.format(self.client_address[0], self.data)
        if not self.desktop:
            self.desktop = DesktopEnvironment()

        self.desktop.onInput(self.data,self.callback)

    def callback(self, data):
        print 'TcpHandler.callback(), data = {}'.format(data)