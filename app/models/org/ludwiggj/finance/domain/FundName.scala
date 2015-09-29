package models.org.ludwiggj.finance.domain

import scala.language.implicitConversions

class FundName private(val name: String) {
  def canEqual(f: FundName) = (name == f.name)

  override def equals(that: Any): Boolean = {
    that match {
      case that: FundName => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }
  }

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + name.hashCode;
    return result
  }

  override def toString = name
}

object FundName {
  def apply(inputName: String) = {
    new FundName(inputName.replaceAll("&amp;", "&").replaceAll("\\^", "").trim)
  }

  implicit def fundNameToString(fundName: FundName) = fundName.name

  implicit def stringToFundName(name: String) = FundName(name)
}