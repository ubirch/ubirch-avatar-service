package com.ubirch.avatar.core.elasticsearch

import com.ubirch.avatar.test.base.UnitSpec
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, JValue}

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class ESClientUtilSpec extends UnitSpec {

  implicit val formats = DefaultFormats.lossless ++ JodaTimeSerializers.all

  feature("searchPath()") {

    scenario("indexes: (); types: ()") {
      ESClientUtil.searchPath(emptySet, emptySet) should be("/_search")
    }

    scenario("indexes: (); types: (23)") {
      ESClientUtil.searchPath(emptySet, set23) should be("/_all/23/_search")
    }

    scenario("indexes: (); types: (23, 42)") {
      ESClientUtil.searchPath(emptySet, set2342) should be("/_all/23,42/_search")
    }

    scenario("indexes: (foo); types: ()") {
      ESClientUtil.searchPath(fooSet, emptySet) should be("/foo/_search")
    }

    scenario("indexes: (foo); types: (23)") {
      ESClientUtil.searchPath(fooSet, set23) should be("/foo/23/_search")
    }

    scenario("indexes: (foo); types: (23, 42)") {
      ESClientUtil.searchPath(fooSet, set2342) should be("/foo/23,42/_search")
    }

    scenario("indexes: (foo, bar); types: ()") {
      ESClientUtil.searchPath(fooBarSet, emptySet) should be("/foo,bar/_search")
    }

    scenario("indexes: (foo, bar); types: (23)") {
      ESClientUtil.searchPath(fooBarSet, set23) should be("/foo,bar/23/_search")
    }

    scenario("indexes: (foo, bar); types: (23, 42)") {
      ESClientUtil.searchPath(fooBarSet, set2342) should be("/foo,bar/23,42/_search")
    }

  }

  feature("uriIndexSection()") {

    scenario("indexes: (); typeDefined: false") {
      ESClientUtil.uriIndexSection(emptySet, false) should be("")
    }

    scenario("indexes: (foo); typeDefined: false") {
      ESClientUtil.uriIndexSection(fooSet, false) should be("/foo")
    }

    scenario("indexes: (foo, bar); typeDefined: false") {
      ESClientUtil.uriIndexSection(fooBarSet, false) should be("/foo,bar")
    }

    scenario("indexes: (); typeDefined: true") {
      ESClientUtil.uriIndexSection(emptySet, true) should be("/_all")
    }

    scenario("indexes: (foo); typeDefined: true") {
      ESClientUtil.uriIndexSection(fooSet, true) should be("/foo")
    }

    scenario("indexes: (foo, bar); typeDefined: true") {
      ESClientUtil.uriIndexSection(fooBarSet, true) should be("/foo,bar")
    }

  }

  feature("uriTypeSection()") {

    scenario("types: (); indexDefined: false") {
      ESClientUtil.uriTypeSection(emptySet, false) should be("")
    }

    scenario("types: (foo); indexDefined: false") {
      ESClientUtil.uriTypeSection(fooSet, false) should be("/foo")
    }

    scenario("types: (foo, bar); indexDefined: false") {
      ESClientUtil.uriTypeSection(fooBarSet, false) should be("/foo,bar")
    }

    scenario("types: (); indexDefined: true") {
      ESClientUtil.uriTypeSection(emptySet, true) should be("")
    }

    scenario("types: (foo); indexDefined: true") {
      ESClientUtil.uriTypeSection(fooSet, true) should be("/foo")
    }

    scenario("types: (foo, bar); indexDefined: true") {
      ESClientUtil.uriTypeSection(fooBarSet, true) should be("/foo,bar")
    }

  }

  feature("simpleQuery()") {

    scenario("search: None, sort: None, limit: None") {
      ESClientUtil.simpleQuery(None, None, None) should be(emptyJValue)
    }

    scenario("search: None, sort: Some, limit: None") {

      // prepare
      val sort = SortSearch("foo")

      // test
      val result = ESClientUtil.simpleQuery(None, Some(sort), None)

      // verify
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      result should be(sortQuery)

    }

    scenario("search: None, sort: None, limit: Some") {

      // prepare
      val limit = 23

      // test
      val result = ESClientUtil.simpleQuery(None, None, Some(limit))

      // verify
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      result should be(limitQuery)

    }

    scenario("search: None, sort: Some, limit: Some") {

      // prepare
      val sort = SortSearch("foo")
      val limit = 23

      // test
      val result = ESClientUtil.simpleQuery(None, Some(sort), Some(limit))

      // verify
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      val expected = render(sortQuery merge limitQuery)
      result should be(expected)

    }

    scenario("search: Some, sort: None, limit: None") {

      // prepare
      val search = SearchField("foo", "bar")

      // test
      val result = ESClientUtil.simpleQuery(Some(search), None, None)

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      result should be(searchQuery)

    }

    scenario("search: Some, sort: Some, limit: None") {

      // prepare
      val search = SearchField("foo", "bar")
      val sort = SortSearch("foo")

      // test
      val result = ESClientUtil.simpleQuery(Some(search), Some(sort), None)

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      val expected = render(searchQuery merge sortQuery)
      result should be(expected)

    }

    scenario("search: Some, sort: None, limit: Some") {

      // prepare
      val search = SearchField("foo", "bar")
      val limit = 23

      // test
      val result = ESClientUtil.simpleQuery(Some(search), None, Some(limit))

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      val expected = render(searchQuery merge limitQuery)
      result should be(expected)

    }

    scenario("search: Some, sort: Some, limit: Some") {

      // prepare
      val search = SearchField("foo", "bar")
      val sort = SortSearch("foo")
      val limit = 23

      // test
      val result = ESClientUtil.simpleQuery(Some(search), Some(sort), Some(limit))

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      val expected = render(searchQuery merge sortQuery merge limitQuery)
      result should be(expected)

    }

  }

  feature("simpleQueryString()") {

    scenario("search: None, sort: None, limit: None") {
      ESClientUtil.simpleQuery(None, None, None) should be(emptyJValue)
    }

    scenario("search: None, sort: Some, limit: None") {

      // prepare
      val sort = SortSearch("foo")

      // test
      val result = ESClientUtil.simpleQueryString(None, Some(sort), None)

      // verify
      val expected =
        """{"sort":{"foo":{"order":"asc"}}}"""
      result should be(expected)

    }

    scenario("search: None, sort: None, limit: Some") {

      // prepare
      val limit = 23

      // test
      val result = ESClientUtil.simpleQueryString(None, None, Some(limit))

      // verify
      val expected =
        """{"size":23}"""
      result should be(expected)

    }

    scenario("search: None, sort: Some, limit: Some") {

      // prepare
      val sort = SortSearch("foo")
      val limit = 23

      // test
      val result = ESClientUtil.simpleQueryString(None, Some(sort), Some(limit))

      // verify
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      val expected = compact(render(sortQuery merge limitQuery)).stripMargin
      result should be(expected)

    }

    scenario("search: Some, sort: None, limit: None") {

      // prepare
      val search = SearchField("foo", "bar")

      // test
      val result = ESClientUtil.simpleQueryString(Some(search), None, None)

      // verify
      val expected =
        """{"query":{"term":{"foo":"bar"}}}"""
      result should be(expected)

    }

    scenario("search: Some, sort: Some, limit: None") {

      // prepare
      val search = SearchField("foo", "bar")
      val sort = SortSearch("foo")

      // test
      val result = ESClientUtil.simpleQueryString(Some(search), Some(sort), None)

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      val expected = compact(render(searchQuery merge sortQuery)).stripMargin
      result should be(expected)

    }

    scenario("search: Some, sort: None, limit: Some") {

      // prepare
      val search = SearchField("foo", "bar")
      val limit = 23

      // test
      val result = ESClientUtil.simpleQueryString(Some(search), None, Some(limit))

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      val expected = compact(render(searchQuery merge limitQuery)).stripMargin
      result should be(expected)

    }

    scenario("search: Some, sort: Some, limit: Some") {

      // prepare
      val search = SearchField("foo", "bar")
      val sort = SortSearch("foo")
      val limit = 23

      // test
      val result = ESClientUtil.simpleQueryString(Some(search), Some(sort), Some(limit))

      // verify
      val searchQuery = ESClientUtil.searchQuery(Some(search))
      val sortQuery = ESClientUtil.sortQuery(Some(sort))
      val limitQuery = ESClientUtil.limitQuery(Some(limit))
      val expected = compact(render(searchQuery merge sortQuery merge limitQuery)).stripMargin
      result should be(expected)

    }

  }

  feature("searchQuery()") {

    scenario("None") {
      ESClientUtil.searchQuery(None) should be(emptyJValue)
    }

    scenario("Some") {

      val searchField = SearchField("foo", "bar")
      val expected = read[JValue](
        s"""{
            |  "query": {
            |    "term": {"${searchField.field}": "${searchField.value}"}
            |  }
            |}""".stripMargin)

      ESClientUtil.searchQuery(Some(searchField)) should be(expected)

    }

  }

  feature("sortQuery()") {

    scenario("None") {
      ESClientUtil.sortQuery(None) should be(emptyJValue)
    }

    scenario("Some(foo, None)") {

      val sort = SortSearch("foo")
      val expected = read[JValue](
        s"""{
            |  "sort": {
            |    "${sort.by}": {"order": "asc"}
            |  }
            |}""".stripMargin)

      ESClientUtil.sortQuery(Some(sort)) should be(expected)

    }

    scenario("Some(foo, asc)") {

      val sort = SortSearch("foo", "asc")
      val expected = read[JValue](
        s"""{
            |  "sort": {
            |    "${sort.by}": {"order": "asc"}
            |  }
            |}""".stripMargin)

      ESClientUtil.sortQuery(Some(sort)) should be(expected)

    }

    scenario("Some(foo, desc)") {

      val sort = SortSearch("foo", "desc")
      val expected = read[JValue](
        s"""{
            |  "sort": {
            |    "${sort.by}": {"order": "desc"}
            |  }
            |}""".stripMargin)

      ESClientUtil.sortQuery(Some(sort)) should be(expected)

    }

    scenario("Some(foo, nonsense)") {

      val sort = SortSearch("foo", "nonsense")
      val expected = read[JValue](
        s"""{
            |  "sort": {
            |    "${sort.by}": {"order": "nonsense"}
            |  }
            |}""".stripMargin)

      ESClientUtil.sortQuery(Some(sort)) should be(expected)

    }

  }

  feature("limitQuery()") {

    scenario("None") {
      ESClientUtil.limitQuery(None) should be(emptyJValue)
    }

    scenario("Some") {
      ESClientUtil.limitQuery(Some(500)) should be(read[JValue](s"""{"size":500}"""))
    }

  }

  private val emptySet: Set[String] = Set.empty
  private val fooSet = Set("foo")
  private val fooBarSet = Set("foo", "bar")
  private val set23 = Set("23")
  private val set2342 = Set("23", "42")

  private val emptyJValue = read[JValue]("{}")

}
