package xieyuheng.minitt

import pretty._

import xieyuheng.util.err._

sealed trait Ctx {

  def lookup(name: String): Option[Val] = {
    this match {
      case CtxVar(name2: String, t: Val, rest: Ctx) =>
        if (name2 == name) {
          Some(t)
        } else {
          rest.lookup(name)
        }
      case CtxEmpty() => None
    }
  }

  def ext(pat: Pat, t: Val, v: Val): Either[Err, Ctx] = {
    pat match {
      case PatVar(name: String) =>
        Right(CtxVar(name, t, this))
      case PatCons(car: Pat, cdr: Pat) =>
        (t, v) match {
          case (ValSigma(arg_t: Val, clo: Clo), v) =>
            for {
              ctx1 <- this.ext(car, arg_t, eval.car(v))
              ctx2 <- ctx1.ext(cdr, clo.ap(eval.car(v)), eval.cdr(v))
            } yield ctx2
          case _ =>
            Left(Err(
              s"[fail to extend ctx]\n" ++
                s"pattern: ${pretty_pat(pat)}\n" ++
                s"type: ${pretty_val(t)}\n" ++
                s"value: ${pretty_val(v)}\n"))
        }
      case PatSole() =>
        Right(this)
    }
  }
}

final case class CtxVar(name: String, t: Val, rest: Ctx) extends Ctx
final case class CtxEmpty() extends Ctx
