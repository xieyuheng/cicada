package xieyuheng.partech

case class Rule(
  name: String,
  choices: Map[String, List[RulePart]],
  args: Map[String, Rule] = Map(),
) {
  if (choices.size == 0) {
    println("Rule should not have empty choices")
    println(s"name: ${name}")
    throw new Exception()
  }

  choices.foreach { case (choice_name, ruleParts) =>
    if (ruleParts.length == 0) {
      println("Rule's choice should not have empty List")
      println(s"name: ${name}")
      println(s"choice: ${choice_name}")
      throw new Exception()
    }
  }

  // - to get lower bound
  // - we can not use `==`, because we can not compare lambda (rule_gen)
  //   but it is ok to mis-comparing some rules to be the same
  //   the lower bound will not be the greatest lower bound

  val matters = (name, choices.keys.toSet, args)

  override def equals(that: Any): Boolean = {
    that match {
      case that: Rule => this.matters == that.matters
      case _ => false
    }
  }

  override def hashCode = matters.hashCode

  lazy val lower_bound: Int = Rule.lower_bound(this, List(this))
}

object Rule {
  def list(name: String, parts: List[RulePart]): Rule = {
    Rule(name, Map(name -> parts))
  }

  private def lower_bound(rule: Rule, occured: List[Rule]): Int = {
    rule.choices.map { case (_name, parts) =>
      parts.foldLeft(0) { case (bound, part) =>
        part match {
          case RulePartStr(str) => bound + 1
          case RulePartRule(rule_gen) =>
            val r = rule_gen()
            if (occured.exists(r == _)) {
              bound
            } else {
              bound + Rule.lower_bound(r, r :: occured)
            }
          case RulePartPred(word_pred) => bound + 1
        }
      }
    }.min
  }
}


sealed trait RulePart

final case class RulePartStr(str: String) extends RulePart {
  override def toString = {
    val doublequote = '"'
    s"${doublequote}${str}${doublequote}"
  }
}

final case class RulePartRule(rule_gen: () => Rule) extends RulePart {
  override def toString = {
    rule_gen().name
  }
}

final case class RulePartPred(word_pred: WordPred) extends RulePart
