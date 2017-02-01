package com.ubirch.transformer.services

import spire.implicits._
import spire.math.Polynomial

/**
  * Created by derMicha on 30/01/17.
  */
object PtxTransformerService {

  case class PTCoefficientStandard(a: BigDecimal, b: BigDecimal, c: BigDecimal)

  /*

   */
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

  private val poly = Polynomial[BigDecimal](k)

  /**
    *
    * @param adcval raw adc resisistence value
    * @return resisistence value
    */
  private def resistance(adcval: BigDecimal): BigDecimal = ((adcval * 1.35 / 65536.0) / 0.0005) / 8.0

  /**
    *
    * @param adc
    * @param standard
    * @return
    */
  def pt100_temperature(adc: BigDecimal, standard: PTCoefficientStandard = ptxITS90): BigDecimal = {
    ptx_temperature(100.0, adc, standard)
  }

  /**
    *
    * @param r0
    * @param adc
    * @param standard
    * @return
    */
  def ptx_temperature(r0: BigDecimal, adc: BigDecimal, standard: PTCoefficientStandard = ptxITS90): BigDecimal = {
    val (a, b) = (standard.a, standard.b)

    val r = resistance(adc)

    val v = ((r0 * r0) * (a * a)) - (4 * r0 * b * (r0 - r))
    val t = ((-r0 * a) + v.sqrt()) / (2.0 * r0 * b)

    if (r < r0) {
      val p = poly(r)


      t + p
    }
    else
      t
  }
}
