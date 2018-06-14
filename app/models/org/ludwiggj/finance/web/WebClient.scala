package models.org.ludwiggj.finance.web

import java.net.{MalformedURLException, URL}
import java.util.logging.Level

import com.gargoylesoftware.htmlunit.{BrowserVersion, IncorrectnessListener, Page, ScriptException, WebRequest}
import com.gargoylesoftware.htmlunit.html.HTMLParserListener
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener
import org.apache.commons.logging.LogFactory
import org.w3c.css.sac.{CSSParseException, ErrorHandler}

class WebClient {
  private def turnLoggingOff: Unit = {
    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")

    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF)
    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF)
  }

  val incorrectnessListener: IncorrectnessListener = (message: String, origin: scala.Any) => {}

  val errorHandler = new ErrorHandler {
    override def warning(exception: CSSParseException): Unit = {}

    override def fatalError(exception: CSSParseException): Unit = {}

    override def error(exception: CSSParseException): Unit = {}
  }

  val javaScriptErrorListener = new JavaScriptErrorListener {
    override def scriptException(arg0: HtmlUnitHtmlPage, arg1: ScriptException): Unit = {}

    override def timeoutError(arg0: HtmlUnitHtmlPage, arg1: Long, arg2: Long): Unit = {}

    override def malformedScriptURL(arg0: HtmlUnitHtmlPage, arg1: String, arg2: MalformedURLException): Unit = {}

    override def loadScriptError(arg0: HtmlUnitHtmlPage, arg1: URL, arg2: Exception): Unit = {}
  }

  val htmlParserListener = new HTMLParserListener {

    override def warning(arg0: String, arg1: URL, arg2: String, arg3: Int, arg4: Int, arg5: String): Unit = {}

    override def error(arg0: String, arg1: URL, arg2: String, arg3: Int, arg4: Int, arg5: String): Unit = {}
  }

  // Start here
  val webClient = new com.gargoylesoftware.htmlunit.WebClient(BrowserVersion.CHROME)

  turnLoggingOff
  webClient.setIncorrectnessListener(incorrectnessListener)
  webClient.setCssErrorHandler(errorHandler)
  webClient.setJavaScriptErrorListener(javaScriptErrorListener)
  webClient.setHTMLParserListener(htmlParserListener)
  webClient.getOptions.setThrowExceptionOnFailingStatusCode(false)
  webClient.getOptions.setThrowExceptionOnScriptError(false)
  webClient.getOptions.setCssEnabled(false)

  def getPage(url: String): HtmlEntity = {
    HtmlPage(webClient.getPage(url))
  }

  def getPage(wr: WebRequest): String = {
    val value: Page = webClient.getPage(wr)
    value.getWebResponse.getContentAsString
  }
}

object WebClient {
  def apply() = new WebClient()
}