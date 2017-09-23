package com.github.gvolpe.smartbackpacker.service

import cats.effect.IO
import io.circe.{Decoder, Json}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.client.blaze._
import org.http4s.circe._

object ExchangeRateApp extends App with ExchangeRateService

trait ExchangeRateService {

  //http://api.fixer.io/latest?base=EUR&symbols=GBP

  //{"base":"EUR","date":"2017-09-22","rates":{"GBP":0.88155}}

  // Free exchange rate API
  // http://fixer.io/

  //implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]

  case class CurrencyExchange(base: String, date: String, rates:  AnyRef)

  val baseCurrency      = "EUR"
  val exchangeCurrency  = "GBP"

  val fixerUri = s"http://api.fixer.io/latest?base=$baseCurrency&symbols=$exchangeCurrency"

  val httpClient = PooledHttp1Client[IO]()
  val result: IO[Json] = httpClient.expect[Json](fixerUri)

  // https://restcountries.eu/rest/v2/alpha/AR?fields=timezones;currencies;languages

//  println(result.unsafeRunSync())

//  val a = httpClient.expect[String]("http://www.airlinequality.com/airline-reviews/ryanair/")
//  a.unsafeRunSync()
//
//  val b = httpClient.expect[String]("http://content.incapsula.com/jsTest.html")
////  println(b.unsafeRunSync())
//
//  val req = Request[IO](
//    uri = Uri(path = "http://www.airlinequality.com/airline-reviews/ryanair/"),
//    headers = Headers(Header("___utvmc", "UA-31107342-1"))
//  )
//
//  val c = httpClient.expect[String](req)
//  println(c.unsafeRunSync())

  // _gaq.push(['_setAccount', 'UA-31107342-1']);

}
