package org.ludwiggj.finance

trait Login {
  def loginAs(accountName: String): HtmlPage
}
