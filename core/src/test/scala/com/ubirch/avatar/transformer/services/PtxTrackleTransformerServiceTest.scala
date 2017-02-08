package com.ubirch.avatar.transformer.services

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.transformer.services.PtxTransformerService
import com.ubirch.util.json.MyJsonProtocol
import org.scalatest.{FeatureSpec, Matchers}

/**
  * Created by derMicha on 30/01/17.
  */
class PtxTrackleTransformerServiceTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  feature("PTX") {
    scenario("temp gt 0") {
      val adc1: BigDecimal = 21285.0
      val t1: BigDecimal = 24.69

      PtxTransformerService.pt100_temperature(adc = adc1).setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe t1
    }

    scenario("temp lt 0") {
      val adc1: BigDecimal = 10.0
      val t1: BigDecimal = -241.91

      PtxTransformerService.pt100_temperature(adc = adc1).setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe t1
    }
  }
}
