package com.github.gvolpe.smartbackpacker.service

import cats.data.EitherT
import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.model._
import com.github.gvolpe.smartbackpacker.repository.algebra.HealthRepository
import org.scalatest.{FlatSpecLike, Matchers}

class HealthServiceSpec extends FlatSpecLike with Matchers {

  private val testHealth = Health(
    vaccinations = Vaccinations(List.empty[Vaccine], List.empty[Vaccine], List.empty[Vaccine]),
    notices = HealthNotices(
      alertLevel = LevelOne,
      alerts = List.empty[HealthAlert]
    )
  )

  private val repo = new HealthRepository[IO] {
    override def findHealthInfo(countryCode: CountryCode): IO[Option[Health]] = IO {
      if (countryCode.value == "AR") Some(testHealth)
      else None
    }
  }

  private val service = new HealthService[IO](repo)

  it should "find the health information" in IOAssertion {
    EitherT(service.findHealthInfo("AR".as[CountryCode])).map { index =>
      index should be (testHealth)
    }.value
  }

  it should "NOT find the health information" in IOAssertion {
    EitherT(service.findHealthInfo("XX".as[CountryCode])).leftMap { error =>
      error shouldBe a [HealthInfoNotFound]
    }.value
  }

}
