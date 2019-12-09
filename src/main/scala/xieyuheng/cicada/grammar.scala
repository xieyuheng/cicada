package xieyuheng.cicada

import collection.immutable.ListMap

import xieyuheng.partech._
import xieyuheng.partech.ruleDSL._
import xieyuheng.partech.predefined._

object grammar {

  val lexer = Lexer.default

  def preserved: List[String] = List(
    "type",
    "class", "define",
    "given", "conclude",
    "let", "return",
  )

  def identifier = identifier_with_preserved("identifier", preserved)

  def exp: Rule = Rule(
    "exp", Map(
      "var" -> List(identifier),
      "type" -> List("type"),
      "pi" -> List("{", non_empty_list(given_entry), "conclude", exp, "}"),
      "fn" -> List("{", non_empty_list(given_entry), "return", exp, "}"),
      "ap" -> List(exp, "(", non_empty_list(arg_entry), ")"),
      "cl" -> List("class", "{", non_empty_list(given_entry), "}"),
      "cl_naked" -> List("{", non_empty_list(given_entry), "}"),
      "cl_empty" -> List("class", "{", "}"),
      "obj" -> List("object", "{", non_empty_list(let_entry), "}"),
      "obj_naked" -> List("{", non_empty_list(let_entry), "}"),
      "obj_empty" -> List("object", "{", "}"),
      "dot" -> List(exp, ".", identifier),
      "block" -> List("{", non_empty_list(let_entry), "return", exp, "}"),
    ))

  def exp_matcher: Tree => Exp = Tree.matcher[Exp](
    "exp", Map(
      "var" -> { case List(Leaf(name)) => Var(name) },
      "type" -> { case List(_) => Type() },
      "pi" -> { case List(_, given_entry_list, _, ret_type, _) =>
        val arg_map = ListMap(non_empty_list_matcher(given_entry_matcher)(given_entry_list): _*)
        Pi(arg_map, exp_matcher(ret_type)) },
      "fn" -> { case List(_, given_entry_list, _, body, _) =>
        val arg_map = ListMap(non_empty_list_matcher(given_entry_matcher)(given_entry_list): _*)
        Fn(arg_map, exp_matcher(body)) },
      "ap" -> { case List(target, _, arg_entry_list, _) =>
        val arg_list = non_empty_list_matcher(arg_entry_matcher)(arg_entry_list)
        Ap(exp_matcher(target), arg_list) },
      "cl" -> { case List(_, _, given_entry_list, _) =>
        val type_map = ListMap(non_empty_list_matcher(given_entry_matcher)(given_entry_list): _*)
        Cl(type_map) },
      "cl_naked" -> { case List(_, given_entry_list, _) =>
        val type_map = ListMap(non_empty_list_matcher(given_entry_matcher)(given_entry_list): _*)
        Cl(type_map) },
      "cl_empty" -> { case List(_, _, _) =>
        Cl(ListMap()) },
      "obj" -> { case List(_, _, let_entry_list, _) =>
        val val_map = ListMap(non_empty_list_matcher(let_entry_matcher)(let_entry_list): _*)
        Obj(val_map) },
      "obj_naked" -> { case List(_, let_entry_list, _) =>
        val val_map = ListMap(non_empty_list_matcher(let_entry_matcher)(let_entry_list): _*)
        Obj(val_map) },
      "obj_empty" -> { case List(_, _, _) =>
        Obj(ListMap()) },
      "dot" -> { case List(target, _, Leaf(field)) =>
        Dot(exp_matcher(target), field) },
      "block" -> { case List(_, let_entry_list, _, body, _) =>
        val let_map = ListMap(non_empty_list_matcher(let_entry_matcher)(let_entry_list): _*)
        Block(let_map, exp_matcher(body)) },
    ))

  def arg_entry = Rule(
    "arg_entry", Map(
      "arg" -> List(exp),
      "arg_comma" -> List(exp, ","),
    ))

  def arg_entry_matcher = Tree.matcher[Exp](
    "arg_entry", Map(
      "arg" -> { case List(exp) => exp_matcher(exp) },
      "arg_comma" -> { case List(exp, _) => exp_matcher(exp) },
    ))

  def given_entry = Rule(
    "given_entry", Map(
      "given" -> List("given", identifier, ":", exp),
    ))

  def given_entry_matcher = Tree.matcher[(String, Exp)](
    "given_entry", Map(
      "given" -> { case List(_, Leaf(name), _, exp) => (name, exp_matcher(exp)) },
    ))

  def let_entry = Rule(
    "let_entry", Map(
      "let" -> List("let", identifier, "=", exp),
    ))

  def let_entry_matcher = Tree.matcher[(String, Exp)](
    "let_entry", Map(
      "let" -> { case List(_, Leaf(name), _, exp) => (name, exp_matcher(exp)) },
    ))

}
