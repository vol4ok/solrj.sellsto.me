package sellstome.util

import org.slf4j.LoggerFactory

/** Adds logging capabilities */
trait Logging {
  val loggerChannelId = this.getClass()
  lazy val logger = LoggerFactory.getLogger(loggerChannelId)

  protected def trace(msg: => String): Unit = {
    if (logger.isTraceEnabled())
      logger.trace(msg)
  }

  protected def trace(e: => Throwable): Any = {
    if (logger.isTraceEnabled())
      logger.trace("",e)
  }

  protected def trace(msg: => String, e: => Throwable) = {
    if (logger.isTraceEnabled())
      logger.trace(msg,e)
  }

  protected def debug(msg: => String): Unit = {
    if (logger.isDebugEnabled())
      logger.debug(msg)
  }

  protected def debug(e: => Throwable): Any = {
    if (logger.isDebugEnabled())
      logger.debug("",e)
  }

  protected def debug(msg: => String, e: => Throwable) = {
    if (logger.isDebugEnabled())
      logger.debug(msg,e)
  }

  protected def info(msg: => String): Unit = {
    if (logger.isInfoEnabled())
      logger.info(msg)
  }

  protected def info(e: => Throwable): Any = {
    if (logger.isInfoEnabled())
      logger.info("",e)
  }

  protected def info(msg: => String, e: => Throwable) = {
    if (logger.isInfoEnabled())
      logger.info(msg,e)
  }

  protected def warn(msg: => String): Unit = {
    logger.warn(msg)
  }

  protected def warn(e: => Throwable): Any = {
    logger.warn("",e)
  }

  protected def warn(msg: => String, e: => Throwable) = {
    logger.warn(msg,e)
  }

  protected def error(msg: => String): Unit = {
    logger.error(msg)
  }

  protected def error(e: => Throwable): Any = {
    logger.error("",e)
  }

  protected def error(msg: => String, e: => Throwable) = {
    logger.error(msg,e)
  }

}
