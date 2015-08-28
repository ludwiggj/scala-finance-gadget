package models.org.ludwiggj.finance.web

trait Login {
  def loginAs(accountName: String): HtmlEntity
}
