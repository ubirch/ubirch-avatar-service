package com.ubirch.avatar.core.elasticsearch

import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object ESClientUtil {

  implicit val formats = DefaultFormats.lossless ++ JodaTimeSerializers.all

  /**
    * Creates the path section of a search URI.
    *
    * @param indexSet indexes to search on (empty list means all)
    * @param typeSet  types to search on (empty list means all)
    * @return
    */
  def searchPath(indexSet: Set[String] = Set.empty,
                 typeSet: Set[String] = Set.empty
                ): String = {

    val typesDefined = typeSet.nonEmpty
    val uriSection = uriIndexSection(indexSet, typesDefined)

    val indexesDefined = indexSet.nonEmpty
    val typeSection = uriTypeSection(typeSet, indexesDefined)

    s"$uriSection$typeSection/_search"

  }

  def uriIndexSection(indexSet: Set[String], typeDefined: Boolean): String = {

    val indexSection = indexSet.mkString(",")

    typeDefined match {
      case true if indexSection == "" => "/_all"
      case false if indexSection == "" => ""
      case _ => s"/$indexSection"
    }

  }

  def uriTypeSection(typeSet: Set[String], indexDefined: Boolean): String = {

    val typeSection = typeSet.mkString(",")

    indexDefined match {
      case true if typeSection == "" => ""
      case false if typeSection == "" => ""
      case _ => s"/$typeSection"
    }

  }

  def simpleQuery(searchOpt: Option[SearchField] = None,
                  sortOpt: Option[SortSearch] = None,
                  limitOpt: Option[Int] = None
                 ): JValue = {

    val search: JValue = searchQuery(searchOpt)
    val sort: JValue = sortQuery(sortOpt)
    val limit: JValue = limitQuery(limitOpt)

    render(search merge sort merge limit)

  }

  def simpleQueryString(searchOpt: Option[SearchField] = None,
                        sortOpt: Option[SortSearch] = None,
                        limitOpt: Option[Int] = None): String = {

    val jvalue = simpleQuery(searchOpt, sortOpt, limitOpt)
    compact(jvalue).stripMargin

  }

  def searchQuery(searchFieldOpt: Option[SearchField]): JValue = {

    searchFieldOpt match {

      case None => emptyJValue

      case Some(searchField) => parse(
        s"""{
            |  "query" :
            |  {
            |    "term" :
            |    { "${searchField.field}" : "${searchField.value}" }
            |  }
            |}""".stripMargin
      )

    }

  }

  def sortQuery(sortOpt: Option[SortSearch]): JValue = {

    sortOpt match {

      case None => emptyJValue

      case Some(sort) => parse(
        s"""
           |{
           | "sort" :
           | {
           |   "${sort.by}":
           |   {
           |     "order" : "${sort.order}"
           |   }
           | }
           |}
         """.stripMargin
      )

    }

  }

  def limitQuery(limitOpt: Option[Int]): JValue = {

    limitOpt match {

      case None => emptyJValue

      case Some(limit) =>
        parse(
          s"""{ "size" : $limit }""".stripMargin
        )

    }

  }

  def emptyJValue: JValue = read[JValue]("{}")

}

case class SearchField(field: String, value: String)

case class SortSearch(by: String, order: String = "asc")
