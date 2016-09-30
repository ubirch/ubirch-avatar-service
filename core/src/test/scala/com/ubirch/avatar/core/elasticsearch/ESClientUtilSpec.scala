package com.ubirch.avatar.core.elasticsearch

import com.ubirch.avatar.test.base.UnitSpec

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class ESClientUtilSpec extends UnitSpec {

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

  private val emptySet: Set[String] = Set.empty
  private val fooSet = Set("foo")
  private val fooBarSet = Set("foo", "bar")
  private val set23 = Set("23")
  private val set2342 = Set("23", "42")

}
