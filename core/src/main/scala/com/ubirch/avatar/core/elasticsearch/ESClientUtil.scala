package com.ubirch.avatar.core.elasticsearch

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object ESClientUtil {

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

  // TODO add query generator

}
