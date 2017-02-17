import simplekml
from polycircles import polycircles

polycircle = polycircles.Polycircle(latitude=40.768085,
                                    longitude=-73.981885,
                                    radius=200,
                                    number_of_vertices=36)
kml = simplekml.Kml()
pol = kml.newpolygon(name="Columbus Circle, Manhattan",
                                         outerboundaryis=polycircle.to_kml())
pol.style.polystyle.color = \
        simplekml.Color.changealphaint(200, simplekml.Color.green)
kml.save("test_kml_polygon_3_manhattan.kml")
