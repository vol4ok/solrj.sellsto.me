package sellstome.search.solr.service.finance

import java.util.concurrent.TimeUnit
import collection.LinearSeq
import reactive.{EventSource, Signal}
import scala.None

/**
 * Created by IntelliJ IDEA.
 * User: Asus
 * Date: 25.01.12
 * Time: 1:27
 * Contract on component that loads a exchange rates over internet
 */
trait RateLoader extends Runnable
                 with    Signal[Option[LinearSeq[ExchangeRate]]] {

  /** contains a current loaded rates */
  private var rates: Option[LinearSeq[ExchangeRate]] = None

  /** Delegates call to a poll() method */
  def run() {
    rates = Some(poll())
    Console.println("Rates loaded")
    emitter.fire(rates)
  }

  /** polls for exchange rates and return a list of values */
  def poll(): LinearSeq[ExchangeRate]

  /** Indicates how often a given rates loader pools for a resource */
  def frequency(): Pair[Long, TimeUnit]

  def now = rates

  val emitter = new EventSource[Option[LinearSeq[ExchangeRate]]] {}
  def change = emitter

}