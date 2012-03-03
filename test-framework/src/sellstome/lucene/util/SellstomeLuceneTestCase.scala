package sellstome.lucene.util

import sellstome.util.AssertionsForJUnit
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.MockDirectoryWrapper
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.util.{SmartRandom, Version, LuceneTestCase}
import org.apache.lucene.index.{LogMergePolicy, IndexWriterConfig, IndexReader}
import org.apache.lucene.document.{FieldType, Field}

/**
 * Scala wrapper for the org.apache.lucene.util.LuceneTestCase
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class SellstomeLuceneTestCase extends LuceneTestCase
                              with AssertionsForJUnit {
  /**
   * Use this constant when creating Analyzers and any other version-dependent stuff.
   * <p><b>NOTE:</b> Change this when development starts for new Lucene version:
   */
  val TestVersionCurrent: Version = LuceneTestCase.TEST_VERSION_CURRENT

  /** Used for generation of random numbers */
  protected val random: SmartRandom = LuceneTestCase.random

  /**
   * create a new searcher over the reader.
   * This searcher might randomly use threads.
   */
  def newSearcher(r: IndexReader): IndexSearcher = LuceneTestCase.newSearcher(r)

  /**
   * Returns a new Directory instance. Use this when the test does not
   * care about the specific Directory implementation (most tests).
   * <p>
   * The Directory is wrapped with [[org.apache.lucene.store.MockDirectoryWrapper]].
   * By default this means it will be picky, such as ensuring that you
   * properly close it and all open files in your test. It will emulate
   * some features of Windows, such as not allowing open files to be
   * overwritten.
   */
  def newDirectory(): MockDirectoryWrapper = LuceneTestCase.newDirectory()

  /** Create a new index writer config with random defaults */
  def newIndexWriterConfig(v: Version, a: Analyzer): IndexWriterConfig = LuceneTestCase.newIndexWriterConfig(v, a)

  def newLogMergePolicy(): LogMergePolicy = LuceneTestCase.newLogMergePolicy()

  def newField(name: String, value: String, fieldType: FieldType): Field = LuceneTestCase.newField(name, value, fieldType)

}