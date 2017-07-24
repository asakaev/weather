# Weather Time Machine


### Server API
* Location(59.8944444, 30.2641667) - Saint-Petersburg
* 27-06-2017 - UTC at 0:00
```
curl http://localhost:8080/locations?city=Saint-Petersburg&city=Moscow
curl http://localhost:8080/history?lat=59.8944444&lon=30.2641667&date=27-06-2017&days=2
```

### WebApp dev
```
sbt webUI/fastOptJS

sbt
project webUI
~fastOptJS
```

### Server dev
```
sbt run
```

### TODO
* Rename project!
* JS build as App
* Build docker image with run end expose (plugin)
* Do not use external types in Weather service
* Share Weather service API types
* PublicApi response/errors docs with json examples
