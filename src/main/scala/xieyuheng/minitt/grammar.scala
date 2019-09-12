package xieyuheng.minitt

import xieyuheng.partech._
import xieyuheng.partech.ruleDSL._
import xieyuheng.partech.predefined._

object grammar {

  val lexer = Lexer.default

  def preserved_identifiers: Set[String] = Set(
    "let", "letrec",
    "sum", "match",
    "trivial",
    "univ",
  )

  def identifier: WordPred = WordPred(
    "identifier", { case word =>
      if (preserved_identifiers.contains(word)) {
        false
      } else {
        word.headOption match {
          case Some(char) =>
            val head_set = lower_case_char_set ++ upper_case_char_set + '_'
            val tail_set = head_set ++ digit_char_set
            head_set.contains(char) && wordInCharSet(tail_set)(word.tail)
          case None => false
        }
      }
    })

  def module = Rule(
    "module", Map(
      "module" -> List(non_empty_list(top)),
    ))

  def module_matcher = Tree.matcher[Module](
    "module", Map(
      "module" -> { case List(top_list) =>
        var module = Module()
        module.top_list = non_empty_list_matcher(top_matcher)(top_list)
        module
      },
    ))

  def top = Rule(
    "top", Map(
      "decl" -> List(decl),
      "eval" -> List("eval", "!", exp),
      "eq" -> List("eq", "!", exp, exp),
      "not_eq" -> List("not_eq", "!", exp, exp),
    ))

  def top_matcher = Tree.matcher[Top](
    "top", Map(
      "decl" -> { case List(decl) => TopDecl(decl_matcher(decl)) },
      "eval" -> { case List(_, _, exp) => TopEval(exp_matcher(exp)) },
      "eq" -> { case List(_, _, x, y) => TopEq(exp_matcher(x), exp_matcher(y)) },
      "not_eq" -> { case List(_, _, x, y) => TopNotEq(exp_matcher(x), exp_matcher(y)) },
    ))

  def decl = Rule(
    "decl", Map(
      "let" -> List("let", pattern, ":", exp, "=", exp),
      "letrec" -> List("letrec", pattern, ":", exp, "=", exp),
    ))

  def decl_matcher = Tree.matcher[Decl](
    "decl", Map(
      "let" -> { case List(_, p, _, e, _, t) =>
        Let(pattern_matcher(p), exp_matcher(e), exp_matcher(t)) },
      "letrec" -> { case List(_, p, _, e, _, t) =>
        Letrec(pattern_matcher(p), exp_matcher(e), exp_matcher(t)) },
    ))

  def exp: Rule = Rule(
    "exp", Map(
      "rator" -> List(rator),
      "non_rator" -> List(non_rator),
    ))

  def exp_matcher: Tree => Exp = Tree.matcher[Exp](
    "exp", Map(
      "rator" -> { case List(rator) => rator_matcher(rator) },
      "non_rator" -> { case List(non_rator) => non_rator_matcher(non_rator) },
    ))

  def rator: Rule = Rule(
    "rator", Map(
      "var" -> List(identifier),
      "ap" ->
        List(rator, "(", non_empty_list(exp_comma), ")"),
      "ap_one_without_comma" ->
        List(rator, "(", exp, ")"),
      "ap_without_last_comma" ->
        List(rator, "(", non_empty_list(exp_comma), exp, ")"),
      "car" -> List(exp, ".", "car"),
      "cdr" -> List(exp, ".", "cdr"),
      "match" -> List("match", "{", non_empty_list(mat_clause), "}"),
    ))

  def rator_matcher: Tree => Exp = Tree.matcher[Exp](
    "rator", Map(
      "var" -> { case List(Leaf(name)) => Var(name) },
      "ap" -> { case List(rator, _, exp_comma_list, _) =>
        non_empty_list_matcher(exp_comma_matcher)(exp_comma_list)
          .foldLeft(rator_matcher(rator)) { case (fn, arg) => Ap(fn, arg) } },
      "ap_one_without_comma" -> { case List(rator, _, exp, _) =>
        Ap(rator_matcher(rator), exp_matcher(exp)) },
      "ap_without_last_comma" -> { case List(rator, _, exp_comma_list, exp, _) =>
        val fn = non_empty_list_matcher(exp_comma_matcher)(exp_comma_list)
          .foldLeft(rator_matcher(rator)) { case (fn, arg) => Ap(fn, arg) }
        Ap(fn, exp_matcher(exp)) },
      "car" -> { case List(exp, _, _) => Car(exp_matcher(exp)) },
      "cdr" -> { case List(exp, _, _) => Cdr(exp_matcher(exp)) },
      "match" -> { case List(_, _, mat_clause_list, _) =>
        Mat(non_empty_list_matcher(mat_clause_matcher)(mat_clause_list).toMap) },
    ))

  def multi_pi_arg: Rule = Rule(
    "multi_pi_arg", Map(
      "arg" -> List(exp),
      "arg_comma" -> List(exp, ","),
      "arg_t" -> List(pattern, ":", exp),
      "arg_t_comma" -> List(pattern, ":", exp, ","),
    ))

  def multi_pi_arg_matcher = Tree.matcher[(Pattern, Exp)](
    "multi_pi_arg", Map(
      "arg" -> { case List(exp) =>
        (SolePattern(), exp_matcher(exp)) },
      "arg_comma" -> { case List(exp, _) =>
        (SolePattern(), exp_matcher(exp)) },
      "arg_t" -> { case List(pattern, _, exp) =>
        (pattern_matcher(pattern), exp_matcher(exp)) },
      "arg_t_comma" -> { case List(pattern, _, exp, _) =>
        (pattern_matcher(pattern), exp_matcher(exp)) },
    ))


  def multi_fn_arg: Rule = Rule(
    "multi_fn_arg", Map(
      "arg" -> List(pattern),
      "arg_comma" -> List(pattern, ","),
    ))

  def multi_fn_arg_matcher = Tree.matcher[Pattern](
    "multi_fn_arg", Map(
      "arg" -> { case List(pattern) =>
        pattern_matcher(pattern) },
      "arg_comma" -> { case List(pattern, _) =>
        pattern_matcher(pattern) },
    ))

  def non_rator: Rule = Rule(
    "non_rator", Map(
      "pi" -> List("(", pattern, ":", exp, ")", "-", ">", exp),
      "multi_pi" -> List("(", non_empty_list(multi_pi_arg), ")", "-", ">", exp),
      "fn" -> List(pattern, "=", ">", exp),
      "multi_fn" -> List("(", non_empty_list(multi_fn_arg), ")", "=", ">", exp),
      "cons" ->
        List("[", non_empty_list(exp_comma), "]"),
      "cons_one_without_comma" ->
        List("[", exp, "]"),
      "cons_without_last_comma" ->
        List("[", non_empty_list(exp_comma), exp, "]"),
      "sigma" -> List("(", pattern, ":", exp, ")", "*", "*", exp),
      "data" -> List(identifier, exp),
      "sum" -> List("sum", "{", non_empty_list(sum_clause), "}"),
      "sole" -> List("[", "]"),
      "trivial" -> List("trivial"),
      "univ" -> List("univ"),
    ))

  def non_rator_matcher: Tree => Exp = Tree.matcher[Exp](
    "non_rator", Map(
      "pi" -> { case List(_, pattern, _, arg_t, _, _, _, t) =>
        Pi(pattern_matcher(pattern), exp_matcher(arg_t), exp_matcher(t)) },
      "multi_pi" -> { case List(_, multi_pi_arg_list, _, _, _, t) =>
        var exp = exp_matcher(t)
        non_empty_list_matcher(multi_pi_arg_matcher)(multi_pi_arg_list)
          .reverse.foreach { case (pattern, arg_t) =>
            exp = Pi(pattern, arg_t, exp)
          }
        exp },
      "fn" -> { case List(pattern, _, _, t) =>
        Fn(pattern_matcher(pattern), exp_matcher(t)) },
      "multi_fn" -> { case List(_, multi_fn_arg_list, _, _, _, t) =>
        var exp = exp_matcher(t)
        non_empty_list_matcher(multi_fn_arg_matcher)(multi_fn_arg_list)
          .reverse.foreach { case pattern =>
            exp = Fn(pattern, exp)
          }
        exp },
      "cons" -> { case List(_, exp_comma_list, _) =>
        val list = non_empty_list_matcher(exp_comma_matcher)(exp_comma_list)
        list.init.foldRight(list.last) { case (head, tail) =>
          Cons(head, tail) } },
      "cons_one_without_comma" -> { case List(_, exp, _) =>
        exp_matcher(exp) },
      "cons_without_last_comma" -> { case List(_, exp_comma_list, exp, _) =>
        val list = non_empty_list_matcher(exp_comma_matcher)(exp_comma_list)
        list.foldRight(exp_matcher(exp)) { case (head, tail) =>
          Cons(head, tail) } },
      "sigma" -> { case List(_, pattern, _, arg_t, _, _, _, t) =>
        Sigma(pattern_matcher(pattern), exp_matcher(arg_t), exp_matcher(t)) },
      "data" -> { case List(Leaf(tag), exp) =>
        Data(tag, exp_matcher(exp)) },
      "sum" -> { case List(_, _, sum_clause_list, _) =>
        Sum(non_empty_list_matcher(sum_clause_matcher)(sum_clause_list).toMap) },
      "sole" -> { case _ => Sole() },
      "trivial" -> { case _ => Trivial() },
      "univ" -> { case _ => Univ() },
    ))

  def exp_comma = Rule(
    "exp_comma", Map(
      "exp_comma" -> List(exp, ","),
    ))

  def exp_comma_matcher = Tree.matcher[Exp](
    "exp_comma", Map(
      "exp_comma" -> { case List(exp, _) => exp_matcher(exp) },
    ))

  def sum_clause = Rule(
    "sum_clause", Map(
      "sum_clause" -> List(identifier, exp, ";"),
    ))

  def sum_clause_matcher: Tree => (String, Exp) = Tree.matcher[(String, Exp)](
    "sum_clause", Map(
      "sum_clause" -> { case List(Leaf(name), t, _) =>
        val exp = exp_matcher(t)
        if (exp == Sole()) {
          (name, Trivial())
        } else {
          (name, exp)
        }
      },
    ))

  def mat_clause = Rule(
    "mat_clause", Map(
      "mat_clause" -> List(identifier, exp, ";"),
    ))

  def mat_clause_matcher: Tree => (String, Exp) = Tree.matcher[(String, Exp)](
    "mat_clause", Map(
      "mat_clause" -> { case List(Leaf(name), exp, _) =>
        (name, exp_matcher(exp)) },
    ))

  def pattern: Rule = Rule(
    "pattern", Map(
      "var" -> List(identifier),
      "cons" ->
        List("[", non_empty_list(pattern_comma), "]"),
      "cons_one_without_comma" ->
        List("[", pattern, "]"),
      "cons_without_last_comma" ->
        List("[", non_empty_list(pattern_comma), pattern, "]"),
      "sole" -> List("[", "]"),
    ))

  def pattern_matcher: Tree => Pattern = Tree.matcher[Pattern](
    "pattern", Map(
      "var" -> { case List(Leaf(name)) => VarPattern(name) },
      "cons" -> { case List(_, pattern_comma_list, _) =>
        val list = non_empty_list_matcher(pattern_comma_matcher)(pattern_comma_list)
        list.init.foldRight(list.last) { case (head, tail) =>
          ConsPattern(head, tail) } },
      "cons_one_without_comma" -> { case List(_, pattern, _) =>
        pattern_matcher(pattern) },
      "cons_without_last_comma" -> { case List(_, pattern_comma_list, pattern, _) =>
        val list = non_empty_list_matcher(pattern_comma_matcher)(pattern_comma_list)
        list.foldRight(pattern_matcher(pattern)) { case (head, tail) =>
          ConsPattern(head, tail) } },
      "sole" -> { case _ => SolePattern() },
    ))

  def pattern_comma = Rule(
    "pattern_comma", Map(
      "pattern_comma" -> List(pattern, ","),
    ))

  def pattern_comma_matcher = Tree.matcher[Pattern](
    "pattern_comma", Map(
      "pattern_comma" -> { case List(pattern, _) => pattern_matcher(pattern) },
    ))
}
