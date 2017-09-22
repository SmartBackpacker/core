package com.github.gvolpe.smartbackpacker

import cats.effect.IO

object WikiAPI {

//  val pageId  = SBConfiguration.pageId("Argentina").getOrElse(0)
  val wikiUrl = "https://en.wikipedia.org/w/api.php?action=parse&format=json&prop=text&pageid="
  //val wikiUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&pageids="

  import org.http4s.client.blaze._

  val httpClient = PooledHttp1Client[IO]()

  //  case class Revision(contentformat: String, contentmodel: String)
  //
  //  case class VisaRequirementsRawJson(query: )

  val travelingTo = "Jordan"

  //  val result: IO[Json] = httpClient.expect[Json](wikiUrl + pageId)
  //  val html = result.map(_.\\("*")).map(_.head).unsafeRunSync()
  //
  //  println(html)

  //  val html = value.split("\"text\":")(1).drop(6).dropRight(4)

  //println(content)
  //  println(doc)
  //
  //  val content = value.split("excluding departure fees")(1).drop(9)
  //
  //  val index = content.indexOf(travelingTo)
  //  val parsed = content.substring(index + travelingTo.length + 8, index + travelingTo.length + 1200).split("}}")
  //
  ////  parsed.foreach(println)
  //
  //  val startingIndex = if (parsed(0).contains("Visa")) 0 else 1
  //
  //  val visaRequirement = parsed(startingIndex).split('|').toList.last
  //  val visaDays =
  //    if (travelingTo == "Gabon") {
  //      parsed(startingIndex + 1).split('\\')(1).drop(3)
  //        .replaceAll("\\[", "")
  //        .replaceAll("\\]", "")
  //        .replaceAll("<br>", " ")
  //        .trim
  //    } else {
  //      parsed(startingIndex + 2).drop(10).split("<ref>")(0).split('\\')(0)
  //        .replaceAll("\\[", "")
  //        .replaceAll("\\]", "")
  //        .replaceAll("<br>", " ")
  //        .split('{')(0)
  //        .trim
  //    }
  //
  //  println(s"$visaRequirement => $visaDays")

  httpClient.shutdownNow()

}
