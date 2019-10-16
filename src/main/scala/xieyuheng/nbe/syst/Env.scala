package xieyuheng.syst

case class Env(map: Map[String, Val] = Map()) {
  def lookup_val(name: String): Option[Val] =
    map.get(name)

  def ext(name: String, value: Val): Env =
    Env(map + (name -> value))
}
