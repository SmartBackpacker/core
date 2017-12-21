package com.github.gvolpe.smartbackpacker.service

import cats.data.EitherT
import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.VisaRestrictionsIndexRepository
import org.scalatest.{FlatSpecLike, Matchers}

class VisaRestrictionsIndexServiceSpec extends FlatSpecLike with Matchers {

  private val testIndex = VisaRestrictionsIndex(new Ranking(3), new Count(2), new Sharing(1))

  private val repo = new VisaRestrictionsIndexRepository[IO] {
    override def findRestrictionsIndex(countryCode: CountryCode): IO[Option[VisaRestrictionsIndex]] = IO {
      if (countryCode.value == "AR") Some(testIndex)
      else None
    }
  }

  private val service = new VisaRestrictionIndexService[IO](repo)

  it should "find the visa restrictions index" in IOAssertion {
    EitherT(service.findIndex("AR".as[CountryCode])).map { index =>
      index should be (testIndex)
    }.value
  }

  it should "NOT find the visa restrictions index" in IOAssertion {
    EitherT(service.findIndex("XX".as[CountryCode])).leftMap { error =>
      error shouldBe a [VisaRestrictionsIndexNotFound]
    }.value
  }

}
