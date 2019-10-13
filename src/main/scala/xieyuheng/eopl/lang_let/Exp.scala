package xieyuheng.eopl.lang_let

sealed trait Exp
final case class Var(name: String) extends Exp
final case class Num(num: Int) extends Exp
final case class Diff(exp1: Exp, exp2: Exp) extends Exp
final case class ZeroP(exp1: Exp) extends Exp
final case class If(exp1: Exp, exp2: Exp, exp3: Exp) extends Exp
final case class Let(name: String, exp1: Exp, body: Exp) extends Exp
final case class Sole() extends Exp
final case class Do(exp1: Exp, body: Exp) extends Exp
final case class AssertEq(exp1: Exp, exp2: Exp) extends Exp
final case class Show(exp1: Exp) extends Exp
