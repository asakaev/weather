package ru.tema.repository

import ru.tema.darksky.Location

import scala.concurrent.Future


class CitiesRepo {

  private val storage = Map(
    "Saint-Petersburg" -> Location(59.8944444, 30.2641667),
    "Moscow" -> Location(55.751244, 37.618423)
  )

  /**
    * Location by city name (dummy)
    */
  def location(city: String): Future[Option[Location]] =
    Future.successful(storage.get(city))
}
