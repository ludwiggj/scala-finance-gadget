package models.org.ludwiggj.finance.domain

class FundName private(val name: String) extends Ordered[FundName] {
  def canEqual(f: FundName): Boolean = (name == f.name)

  override def equals(that: Any): Boolean = {
    that match {
      case that: FundName => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }
  }

  override def hashCode: Int = {
    if (name == null) 0 else name.hashCode
  }

  override def toString: String = name

  override def compare(that: FundName): Int = {
    this.name.compareTo(that.name)
  }
}

object FundName {
  def apply(inputName: String): FundName = {
    new FundName(inputName.replaceAll("&amp;", "&").replaceAll("\\^", "").trim)
  }
}