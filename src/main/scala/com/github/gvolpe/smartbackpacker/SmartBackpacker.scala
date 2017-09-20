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

  val travelingTo = "China"

  val result: IO[Json] = httpClient.expect[Json](wikiUrl + pageId)
  val value = result.unsafeRunSync().noSpaces

  val content = value.split("excluding departure fees")(1).drop(9)

  val index = content.indexOf(travelingTo)
  val parsed = content.substring(index + travelingTo.length + 8, index + travelingTo.length + 1200).split("}}")

  parsed.foreach(println)

  val startingIndex = if (parsed(0).contains("Visa")) 0 else 1

  val visaRequirement = parsed(startingIndex).split('|').toList.last
  val visaDays = parsed(startingIndex + 2).drop(10).split("<ref>")(0).split('\\')(0)
    .replaceAll("\\[", "")
    .replaceAll("\\]", "")
    .replaceAll("<br>", " ")
    .split('{')(0)
    .trim

  println(s"$visaRequirement => $visaDays")

  httpClient.shutdownNow()

}
