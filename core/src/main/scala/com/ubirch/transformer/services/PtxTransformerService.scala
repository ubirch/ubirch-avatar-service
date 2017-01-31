package com.ubirch.transformer.services

import spire.implicits._
import spire.math.Polynomial

/**
  * Created by derMicha on 30/01/17.
  */
object PtxTransformerService {

  case class PTCoefficientStandard(a: BigDecimal, b: BigDecimal, c: BigDecimal)

  private val ptxITS90 = PTCoefficientStandard(BigDecimal(3.9083E-03), BigDecimal(-5.7750E-07), BigDecimal(-4.1830E-12))
  //  val ptxITS90 = PTCoefficientStandard(dec"3.9083".exp(), BigDecimal(dec"-5.7750"), BigDecimal(dec"-4.1830"))

  private val k = Map[Int, BigDecimal](
    5 -> 1.51892983E-10,
    4 -> -2.85842067E-08,
    3 -> -5.34227299E-06,
    2 -> 1.80282972E-03,
    1 -> -1.61875985E-01,
    0 -> 4.84112370
  )
  private val poly = Polynomial[BigDecimal](
    k
  )

  private def resistance(adcval: BigDecimal): BigDecimal = ((adcval * 1.35 / 65536.0) / 0.0005) / 8.0

  def pt100_temperature(r: BigDecimal, standard: PTCoefficientStandard = ptxITS90): BigDecimal = {
    ptx_temperature(100.0, r, standard)
  }

  def ptx_temperature(r0: BigDecimal, r: BigDecimal, standard: PTCoefficientStandard = ptxITS90): BigDecimal = {
    val (a, b) = (standard.a, standard.b)

    val res = resistance(r)

    val v = ((r0 * r0) * (a * a)) - (4 * r0 * b * (r0 - res))
    val t = ((-r0 * a) + v.sqrt()) / (2.0 * r0 * b)

    if (res < r0) {
      val p = poly(res)


      t + p
    }
    else
      t
  }
}
