package sellstome.util

import org.slf4j.LoggerFactory

/** Adds logging capabilities */
trait Logging {
  val loggerChannelId = this.getClass()
  lazy val logger = LoggerFactory.getLogger(loggerChannelId)

  def trace(msg: => String): Unit = {
    if (logger.isTraceEnabled())
      logger.trace(msg)
  }

  def trace(e: => Throwable): Any = {
    if (logger.isTraceEnabled())
      logger.trace("",e)
  }

  def trace(msg: => String, e: => Throwable) = {
    if (logger.isTraceEnabled())
      logger.trace(msg,e)
  }

  def debug(msg: => String): Unit = {
    if (logger.isDebugEnabled())
      logger.debug(msg)
  }

  def debug(e: => Throwable): Any = {
    if (logger.isDebugEnabled())
      logger.debug("",e)
  }

  def debug(msg: => String, e: => Throwable) = {
    if (logger.isDebugEnabled())
      logger.debug(msg,e)
  }

  def info(msg: => String): Unit = {
    if (logger.isInfoEnabled())
      logger.info(msg)
  }

  def info(e: => Throwable): Any = {
    if (logger.isInfoEnabled())
      logger.info("",e)
  }

  def info(msg: => String, e: => Throwable) = {
    if (logger.isInfoEnabled())
      logger.info(msg,e)
  }

  def warn(msg: => String): Unit = {
    logger.warn(msg)
  }

  def warn(e: => Throwable): Any = {
    logger.warn("",e)
  }

  def warn(msg: => String, e: => Throwable) = {
    logger.warn(msg,e)
  }

  def error(msg: => String): Unit = {
    logger.error(msg)
  }

  def error(e: => Throwable): Any = {
    logger.error("",e)
  }

  def error(msg: => String, e: => Throwable) = {
    logger.error(msg,e)
  }

}
