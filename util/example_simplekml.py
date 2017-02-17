import simplekml
kml = simplekml.Kml()
kml.newpoint(name="Kirstenbosch", coords=[(18.432314,-33.988862)])  # lon, lat, optional height
kml.save("botanicalgarden.kml")
