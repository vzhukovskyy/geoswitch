import sys
import re
import simplekml
from polycircles import polycircles
import datetime


def addCircleToKml(kml, lat, lon, radius, name, color):
    polycircle = polycircles.Polycircle(latitude=float(lat),
                                        longitude=float(lon),
                                        radius=float(radius),
                                        number_of_vertices=36)
    pol = kml.newpolygon(name=name, outerboundaryis=polycircle.to_kml())
    pol.style.polystyle.color = simplekml.Color.changealphaint(100, color)
    kml.newpoint(name=name, coords=[(float(lon), float(lat))])

def addLineStringtoKml(coordinates_arr, begin, end):
    ls = kml.newlinestring()
    ls.coords = coordinates_arr
    ls.style.linestyle.width = 5
    ls.style.linestyle.color = simplekml.Color.red
    ls.timespan.begin = begin.isoformat('T')
    ls.timespan.end = end.isoformat('T')
    ls.description = begin.isoformat(' ')+'\n-\n'+end.isoformat(' ')

    # pnt = kml.newpoint(coords=[coordinates_arr[0]])
    # pnt.timestamp.when = begin.isoformat('T')
    # pnt.description = str(begin)
    # pnt = kml.newpoint(coords=[coordinates_arr[-1]])
    # pnt.timestamp.when = end.isoformat('T')
    # pnt.description = str(end)


kml = simplekml.Kml()

logFilename = sys.argv[1]
dotPos = logFilename.rfind(".")
kmlfilename = logFilename[:dotPos]+'.kml'

triggers = []
coordinates_arr = []
start_dt = None
prev_dt = None

with open(logFilename) as inputfile:
    for line in inputfile:
        if " L " in line:
            date, time, tagL, lat, lon, tagAcc, acc = line.split()
            
            dt = datetime.datetime.strptime(str(date)+" "+time, "%Y-%m-%d %H:%M:%S.%f")
            if prev_dt is None:
                start_dt = dt
                prev_dt = dt
            else:
                delta = dt-prev_dt
                if delta < datetime.timedelta(seconds=3):
                    coordinates_arr.append((lon,lat))
                else:
                    addLineStringtoKml(coordinates_arr, start_dt, prev_dt)
                    coordinates_arr = [(lon,lat)]
                    start_dt = dt
                prev_dt = dt
                #print(delta)
            # pnt = kml.newpoint(coords=[(lon, lat)])
            # pnt.timestamp.when = date+'T'+time
            # pnt.description = date+' '+time
        elif " T " in line:
            elems = line.split()
            triggerType = elems[3]
            if triggerType == 'Exit' or triggerType == 'Enter':
                # Exit (50.3956463,30.6331508) R=100.0
                print(elems)
                lat, lon = re.findall(r'\d+\.*\d*', elems[4])
                radius = re.findall(r'\d+\.*\d*', elems[5])[0]
                triggers.append((triggerType, lat, lon, radius))
            if triggerType == 'Transition':
                # Transition from (50.195372693939575,30.665469020605087) R=76.76939900716145 to (50.19567103468903,30.66701330244541) R=76.76939900716145
                latFrom, lonFrom = re.findall(r'\d+\.*\d*', elems[5])
                radius = re.findall(r'\d+\.*\d*', elems[6])[0]
                latTo, lonTo = re.findall(r'\d+\.*\d*', elems[8])
                triggers.append((triggerType, latFrom, lonFrom, latTo, lonTo, radius))

addLineStringtoKml(coordinates_arr, start_dt, prev_dt)

triggers = list(set(triggers))
print(triggers)

for trigger in triggers:
    if trigger[0] == 'Exit' or trigger[0] == 'Enter':
        addCircleToKml(kml, lat=trigger[1], lon=trigger[2], radius=trigger[3], name=trigger[0], color=simplekml.Color.yellow)
    if trigger[0] == 'Transition':
        addCircleToKml(kml, lat=trigger[1], lon=trigger[2], radius=trigger[5], name='From', color=simplekml.Color.yellow)
        addCircleToKml(kml, lat=trigger[3], lon=trigger[4], radius=trigger[5], name='To', color=simplekml.Color.pink)

kml.save(kmlfilename)



