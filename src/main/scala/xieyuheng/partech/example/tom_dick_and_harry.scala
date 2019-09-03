package xieyuheng.partech.example

import xieyuheng.partech._
import xieyuheng.partech.dsl._
import xieyuheng.partech.example.collection._

object tom_dick_and_harry extends ExampleRule {

  val sentences = Seq(
    "tom, dick and harry",
  )

  val non_sentences = Seq(
    "tom, dick, harry",
  )

  def main = Rule(
    "tom_dick_and_harry", Map(
      "name" -> Seq(name),
      "list" -> Seq(name_list, " and ", name)))

  def name = Rule(
    "name", Map(
      "tom" -> Seq("tom"),
      "dick" -> Seq("dick"),
      "harry" -> Seq("harry")))

  // def name_list: Rule = Rule(
  //   "name_list", Map(
  //     "one" -> Seq(name),
  //     "more" -> Seq(name, ", ", name_list)))

  def name_list = non_empty_list(name)(", ")

}
