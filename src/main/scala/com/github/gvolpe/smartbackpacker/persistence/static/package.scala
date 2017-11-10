package com.github.gvolpe.smartbackpacker.persistence

import com.github.gvolpe.smartbackpacker.model.BaggageAllowance

package object static {

  type CreateBaggageAllowanceDTO = (Int, String, Option[Int], Int, Int, Int)

  implicit class BaggageAllowanceOps(baggageAllowance: List[BaggageAllowance]) {
    def toDTO(policyId: Int): List[CreateBaggageAllowanceDTO] = {
      baggageAllowance.map { ba =>
        (policyId, ba.baggageType.toString, ba.kgs, ba.size.height, ba.size.width, ba.size.depth)
      }
    }
  }

}
