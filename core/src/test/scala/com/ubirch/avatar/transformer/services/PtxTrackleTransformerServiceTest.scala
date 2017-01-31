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
    scenario("simple case") {
      val adc1: BigDecimal = 21285.0
      val adc2: BigDecimal = 10.0

      val t1: BigDecimal = 24.69
      val t2: BigDecimal = -241.91

      PtxTransformerService.pt100_temperature(r = adc1).setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe t1
      PtxTransformerService.pt100_temperature(r = adc2).setScale(2, BigDecimal.RoundingMode.HALF_UP) shouldBe t2
    }
  }
}
