package ru.tema

import com.typesafe.config.ConfigFactory
import ru.tema.darksky.{ DarkSkyClient, DarkSkyDummy }
import ru.tema.repository.CitiesRepo
import ru.tema.stats.StatsCalc
import ru.tema.weather.{ PublicApi, WeatherService, WebServer }


object Application extends App {
  val conf = ConfigFactory.load
  val apiKey = conf.getString("dark-sky.api-key")
  val host = conf.getString("web-server.host")
  val port = conf.getInt("web-server.port")

//  val darkSkyClient = new DarkSkyClient(apiKey)
  val darkSkyClient = new DarkSkyDummy

  val publicApi = new PublicApi(
    new WeatherService(darkSkyClient, StatsCalc),
    new CitiesRepo
  )

  new WebServer(host, port, publicApi)
}
