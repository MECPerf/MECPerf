class Measure:
    def __init__(self,x, y, noise):
        self.x = x
        self.y = y
        self.noise = noise
        self.dashfilename = None

    def __init__(self,x, y, noise, dashfilename):
        self.x = x
        self.y = y
        self.noise = noise
        self.dashfilename = dashfilename