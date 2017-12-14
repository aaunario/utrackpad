import pyautogui


class DesktopEnvironment(object):

    INPUT_POINTER_PUSHMOD = 20
    _isActive = True
    callback = None
    gui = pyautogui

    # Make class singleton
    def __new__(cls, *args, **kwargs):
        it = cls.__dict__.get("__it__")
        if it is not None:
            return it
        cls.__it__ = it = object.__new__(cls)
        it.init(*args, **kwargs)
        return it

    def init(self, *args, **kwargs):
        self.gui.PAUSE = 0.0
        self.gui.FAILSAFE = False
        pass

    def onInput(self, data, callback):
        self.callback = callback
        inputHandlers = dict(p=self.onPointerInput, d=self.onUberDesktopInput, k=self.onKeyboardInput)

        # string -> array conversion
        data = data.split()
        data.reverse()
        type = data.pop()
        cmd = data.pop()
        handler = inputHandlers[type]
        data.reverse()
        data = dict(cmd=cmd, args=data, gui=pyautogui)
        handler.__call__(data)

    def onUberDesktopInput(self, cmd):
        print 'uberdesktop input detected'
        args = cmd.get('args')
        gui = cmd.get('gui')
        cmd = cmd.get('cmd')

        if self.isValidUberDesktopCmd(cmd, args):
            if cmd == 'get':
                self.getGuiProperty(gui, args)
            elif cmd == 'set':
                self.setGuiProperty(gui, args)


    def getGuiProperty(self, gui, args):
        result = ''
        for arg in args:
            if arg == 'size':
                result += gui.size().split().join(' ')
            elif arg == 'pLoc':
                result += gui.position().split().join(' ')

        self.callback.__call__(result)

    def setGuiProperty(self, gui, args):
        result = None
        for arg in args:
            if arg == 'on':
                self.activate(args[arg])

    def isValidUberDesktopCmd(self, cmd, args):

        (get, set, on, size, w, h, loc, x, y) = ('get', 'set', 'on', 'size', 'w', 'h', 'loc', 'x', 'y')
        valid_args_map = {get: (on, size, w, h, loc, x, y), set: {on: (0,1)}}

        valid_args = valid_args_map[cmd]
        valid = True
        for arg in args:
            valid &= arg in valid_args
            if not valid:
                break
        return valid

    def onKeyboardInput(self, args):
        if not self.isActive(): return

    def onPointerInput(self, cmd):
        if not self.isActive(): return

        args = cmd.get('args')
        gui = cmd.get('gui')
        cury = args.pop()
        curx = args.pop()
        vec2d = [curx, cury]
        move = []
        for val in vec2d:
            index = vec2d.index(val)
            vec2d[index] = float(val)
            move.insert(index, self.INPUT_POINTER_PUSHMOD * vec2d[index])

        # Map methods and corresponding args to valid cmd strings
        (mv, dr, sk, dk, rk) = ('mv', 'dr', 'sk', 'dk', 'rk')
        methods_map = {mv:gui.moveRel, dr:gui.dragRel, sk:gui.click, dk:gui.doubleClick, rk:gui.rightClick}
        args_map = {mv:move, dr:move, sk:[], dk:[], rk:[]}
        cmd = cmd.get('cmd')
        method = methods_map[cmd]
        args = args_map[cmd]

        method.__call__(args)

    def screenSize(self):
        return pyautogui.size()

    def isActive(self):
        return self._isActive

    def activate(self, val):
        self._isActive = bool(int(val))
