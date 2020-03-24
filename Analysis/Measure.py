class Measure:
    def __init__(self,x, y, noise):
        self.x = x
        self.y = y
        self.noise = noise



class PassiveMeasure(Measure):
    def __init__(self,x, y, noise, dashfilename, numberofclients, startexperiment_timestamp):
        Measure.__init__(self, x, y, noise)
        self.dashfilename = dashfilename
        self.numberofclients =numberofclients
        self.startexperiment_timestamp= startexperiment_timestamp