package com.github.gvolpe.smartbackpacker

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.SmartBackpacker.browser
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import io.circe.Json

object SmartBackpacker extends App {

  println("Smart Backpacker API")

  val pageId  = SBConfiguration.pageId("Argentina").getOrElse(0)
  val wikiUrl = "https://en.wikipedia.org/w/api.php?action=parse&format=json&prop=text&pageid="
  //val wikiUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&pageids="

  import org.http4s.client.blaze._
  import org.http4s.circe._

  val httpClient = PooledHttp1Client[IO]()

//  case class Revision(contentformat: String, contentmodel: String)
//
//  case class VisaRequirementsRawJson(query: )

  val travelingTo = "Jordan"

  val result: IO[Json] = httpClient.expect[Json](wikiUrl + pageId)
  val value = result.unsafeRunSync().noSpaces

  val html = value.split("\"text\":")(1).drop(6).dropRight(4)

  import net.ruippeixotog.scalascraper.browser.JsoupBrowser

  val browser = JsoupBrowser()

  val doc = browser.parseString(html)

  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
  import net.ruippeixotog.scalascraper.model._

  // Extract the text inside the element with id "header"
  val table = doc >> extractor("table")

  println(table)

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
