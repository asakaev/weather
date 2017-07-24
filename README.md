# Weather Time Machine

![Back to the Future](http://cdn.makeuseof.com/wp-content/uploads/2015/09/0635-precise-weather-forecasts.png)

HTTP API and Web application with UI to check weather history.


### Run Webserver
```
???
```

### WebApp usage
1. Run any http static server from `weather/web-ui/bin` dir
2. Open `http://localhost:8000/` in browser
```
python -m SimpleHTTPServer 8000
php -S 127.0.0.1:8000
ruby -run -ehttpd . -p8000
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

### WebApp build
1. `fullOptJS`
2. copy `webui-opt.js`


### TODO
* Remove bin from repo
* JS build as App
* Build docker image with run end expose (plugin)
* Do not use external types in Weather service
* Share Weather service API types
* PublicApi response/errors docs with json examples
* OpenAPI (Swagger) spec and Scala codegen?
* .js pipeline as standalone HTML+CSS+JS app
* Rename code entities to *timemachine related
* Simplify client/server interaction: Service abstraction (Finagle inspired)
