import socket
from Tkinter import *

class PhoneUIEmulator(Frame):

    HOST = "127.0.0.1"
    PORT = 50047
    s = None

    def __init__(self, master=None):
        master.bind
        Frame.__init__(self, master)
        self.pack()
        self.createWidgets()
        self.bindArrowKeys(master)

    def destroy(self):
        BaseWidget.destroy(self)
        if self.s is not None:
            self.s.close()
            self.s = None

    def bindArrowKeys(self, window):
        window.bind('<Left>', lambda arg: self.sendMessage('p move -10 0'))
        window.bind('<Right>', lambda arg: self.sendMessage('p move 10 0'))
        window.bind('<Down>', lambda arg: self.sendMessage('p move 0 10'))
        window.bind('<Up>', lambda arg: self.sendMessage('p move 0 -10'))

    def sendMessage(self, msg):
        print 'CLIENT msg = ' + msg
            # self.s.connect((self.HOST, self.PORT))
        try:
            self.s.sendall(msg)
        except:
            print 'TEST CLIENT sending failed'

    def createWidgets(self):
        self.CONNECT = Button(self, text='connect', fg='red', command=self.onConnectClick)
        self.CONNECT.pack(side=LEFT)

        self.SENDMSG = Button(self, text='send message', fg='red', command=self.onSendMsgClick)
        self.SENDMSG.pack(side=BOTTOM)

        self.QUIT = Button(self, text='exit', fg='red', command=self.onCloseClick)
        self.QUIT.pack(side=RIGHT)


    def connect(self):
        for res in socket.getaddrinfo( self.HOST, self.PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
            af, socktype, proto, canonname, sa = res
            try:
                self.s = socket.socket(af, socktype, proto)
            except socket.error as msg:
                self.s = None
                continue
            if self.s:
                self.s.settimeout(5.0)
                self.s.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
                self.s.setsockopt(socket.SOL_TCP, socket.TCP_KEEPIDLE, 60)
                self.s.setsockopt(socket.SOL_TCP, socket.TCP_KEEPCNT, 4)
                self.s.setsockopt(socket.SOL_TCP, socket.TCP_KEEPINTVL, 15)
            try:
                self.s.connect(sa)
            except socket.error as msg:
                self.s.close()
                self.s = None
                continue
            break
        if self.s is None:
            print 'CLIENT: could not open socket'
            sys.exit(1)
        else:
            print 'Connection made'

    def onConnectClick(self):
        self.connect()

    def onCloseClick(self):
        print 'CLIENT closing connection'
        if self.s:
            self.s.close()
    def onSendMsgClick(self):
        self.sendMessage("TESTICLES 1 2 ....")

def runGUIClient():
    root = Tk()
    app = PhoneUIEmulator(master=root)
    root.mainloop()
    # root.destroy()
    return app