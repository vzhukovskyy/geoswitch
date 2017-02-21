import sys
import re
import simplekml
from polycircles import polycircles



def addCircleToKml(kml, lat, lon, radius, name, color):
    polycircle = polycircles.Polycircle(latitude=float(lat),
                                        longitude=float(lon),
                                        radius=float(radius),
                                        number_of_vertices=36)
    pol = kml.newpolygon(name=name, outerboundaryis=polycircle.to_kml())
    pol.style.polystyle.color = simplekml.Color.changealphaint(100, color)
    kml.newpoint(name=name, coords=[(float(lon), float(lat))])



kml = simplekml.Kml()

logFilename = sys.argv[1]
dotPos = logFilename.rfind(".")
kmlfilename = logFilename[:dotPos]+'.kml'

triggers = []

with open(logFilename) as inputfile:
    for line in inputfile:
        if " L " in line:
            date, time, tagL, lat, lon, tagAcc, acc = line.split()
            pnt = kml.newpoint(coords=[(lon, lat)])
            pnt.timestamp.when = date+'T'+time
            pnt.description = date+' '+time
        elif " T " in line:
            elems = line.split()
            triggerType = elems[3]
            if triggerType == 'Exit' or triggerType == 'Enter':
                # Exit (50.3956463,30.6331508) R=100.0
                lat, lon = re.findall(r'\d+\.*\d*', elems[5])
                radius = re.findall(r'\d+\.*\d*', elems[6])[0]
                triggers.append((triggerType, lat, lon, radius))
            if triggerType == 'Transition':
                # Transition from (50.195372693939575,30.665469020605087) R=76.76939900716145 to (50.19567103468903,30.66701330244541) R=76.76939900716145
                latFrom, lonFrom = re.findall(r'\d+\.*\d*', elems[5])
                radius = re.findall(r'\d+\.*\d*', elems[6])[0]
                latTo, lonTo = re.findall(r'\d+\.*\d*', elems[8])
                triggers.append((triggerType, latFrom, lonFrom, latTo, lonTo, radius))


triggers = list(set(triggers))
print(triggers)

for trigger in triggers:
    if trigger[0] == 'Exit' or trigger[0] == 'Enter':
        addCircleToKml(kml, lat=trigger[1], lon=trigger[2], radius=trigger[3], name=trigger[0], color=simplekml.Color.yellow)
    if trigger[0] == 'Transition':
        addCircleToKml(kml, lat=trigger[1], lon=trigger[2], radius=trigger[5], name='From', color=simplekml.Color.yellow)
        addCircleToKml(kml, lat=trigger[3], lon=trigger[4], radius=trigger[5], name='To', color=simplekml.Color.pink)

kml.save(kmlfilename)



