package xieyuheng.syst

sealed trait Decl
final case class DeclLet(name: String, t: Type, e: Exp) extends Decl
