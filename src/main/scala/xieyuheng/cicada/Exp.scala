package xieyuheng.cicada

import collection.immutable.ListMap

sealed trait Exp
final case class Var(name: String) extends Exp
final case class Type() extends Exp
final case class Pi(arg_map: ListMap[String, Exp], ret_type: Exp) extends Exp
final case class Fn(arg_map: ListMap[String, Exp], body: Exp) extends Exp
final case class Ap(target: Exp, arg_list: List[Exp]) extends Exp
final case class Cl(type_map: ListMap[String, Exp]) extends Exp
final case class Obj(val_map: ListMap[String, Exp]) extends Exp
final case class Dot(target: Exp, field: String) extends Exp
final case class Block(let_map: ListMap[String, Exp], body: Exp) extends Exp
