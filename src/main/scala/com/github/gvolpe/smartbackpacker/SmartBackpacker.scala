package com.github.gvolpe.smartbackpacker

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import io.circe.Json

object SmartBackpacker extends App {

  println("Smart Backpacker API")

  val pageId  = SBConfiguration.pageId("Argentina").getOrElse(0)
  val wikiUrl = s"https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&pageids="

  import org.http4s.client.blaze._
  import org.http4s.circe._

  val httpClient = PooledHttp1Client[IO]()


//  case class Revision(contentformat: String, contentmodel: String)
//
//  case class VisaRequirementsRawJson(query: )

  val result: IO[Json] = httpClient.expect[Json](wikiUrl + pageId)
  val value = result.unsafeRunSync().noSpaces

  println(value.split("excluding departure fees")(1).drop(9))

  httpClient.shutdownNow()

}
