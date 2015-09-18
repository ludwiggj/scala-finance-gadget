package models.org.ludwiggj.finance.web

trait Login {
  def loginAs(userName: String): HtmlEntity
}
