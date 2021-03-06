# Weather Time Machine

![Back to the Future](http://cdn.makeuseof.com/wp-content/uploads/2015/09/0635-precise-weather-forecasts.png)

HTTP API and Web application with UI to check weather history.


### Service build
```
sbt docker
```

### Run Webserver
```
docker run -p 8080:8080 weather/weather
```

### WebApp usage
1. Run static http server from project dir
2. Open `http://localhost:8000/` in browser
```
docker run -p 8000:80 -v "$PWD/web-ui/bin":/usr/local/apache2/htdocs/ httpd:2.4-alpine
```


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


### WebApp build (WIP)
1. Build JS using `fullOptJS`
2. Copy `webui-opt.js`, `webui-jsdeps.js` to `bin`


### TODO
* Unit tests BE
* Stats bugfixes
* DarkSky service HTTP fixed size connection pool (5 persistent tcp connections?)
* Remove bin from repo
* Cleanup index.html from CSS
* Replace DOM/JQuery with React
* Logging, get rid of print lines
* Build docker image with run end expose (plugin)
* Do not use external types in Weather service
* Share Weather service model (API) with server and client
* Public Api response/errors documentation with json examples
* OpenAPI (Swagger) spec and Scala codegen?
* .js pipeline as standalone HTML+CSS+JS app
* Rename code entities to *timemachine related things
* Simplify client/server interaction: Service abstraction (Finagle inspired)
