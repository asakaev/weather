package ru.tema.darksky

import java.time.LocalDateTime

import scala.concurrent.Future


trait DarkSkyService {

  /**
    * A Time Machine Request returns the observed (in the past) or forecasted (in the future)
    * hour-by-hour weather and daily weather conditions for a particular date.
    *
    * @param location Latitude and longitude (in decimal degrees)
    * @param time UNIX time (timezone should be omitted to refer to local time for the location being requested)
    * @return
    */
  def history(location: Location, time: LocalDateTime): Future[Response]
}
