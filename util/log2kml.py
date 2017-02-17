import sys
import locale

filename = sys.argv[1]
print(filename)
dotPos = filename.rfind(".")
name = filename[:dotPos]
outfilename = name+'.kml'
print(outfilename)
outfile = open(outfilename, "w+")
        
print("<?xml version='1.0' encoding='UTF-8'?>", file=outfile)
print("<kml xmlns='http://earth.google.com/kml/2.1'>", file=outfile)
print("<Document>", file=outfile)
print("   <name>" + outfilename +"</name>", file=outfile)

with open(filename) as inputfile:
    for line in inputfile:
        if " L " in line:
            date, time, tagL, lat, lon, tagAcc, acc = line.split()
            print("   <Placemark>", file=outfile)
            # print("       <name>" + time + "</name>", file=outfile)
            # print("       <description>" + date + "</description>", file=outfile)
            print("       <Point>", file=outfile)
            print("           <coordinates>" + lon + "," + lat + "," + "0" + "</coordinates>", file=outfile)
            print("       </Point>", file=outfile)
            print("       <TimeStamp>", file=outfile)
            print("           <when>"+date+'T'+time+"</when>", file=outfile)
            print("       </TimeStamp>", file=outfile)
            print("   </Placemark>", file=outfile)

print("</Document>", file=outfile)
print("</kml>", file=outfile)

outfile.close()
