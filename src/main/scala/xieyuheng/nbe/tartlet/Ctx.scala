package xieyuheng.tartlet

case class Ctx(map: Map[String, Den] = Map()) {
  def lookup_den(name: String): Option[Den] =
    map.get (name)

  def lookup_type(name: String): Option[Val] =
    map.get (name) match {
      case Some(Def(t, value)) =>
        Some(t)
      case Some(Bind(t)) =>
        Some(t)
      case None =>
        None
    }

  def lookup_val(name: String): Option[Val] =
    map.get (name) match {
      case Some(Def(t, value)) =>
        Some(t)
      case Some(Bind(t)) =>
        None
      case None =>
        None
    }

  def ext(name: String, den: Den): Ctx =
    Ctx(map + (name -> den))

  def names: Set[String] = map.keySet

  def to_env: Env = {
    val valueMap = map.map {
      case (name, Def(t, value)) => (name, value)
      case (name, Bind(t)) => (name, TheNeu(t, NeuVar(name)))
    }
    Env(valueMap)
  }
}
