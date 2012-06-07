package sellstome.util

import org.junit.Assert

/**
 * Allows using JUnit assertions methods.
 * Delegates to the [[org.junit.Assert]]
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
trait AssertionsForJUnit {

  def assertTrue(message: String, condition: Boolean) = Assert.assertTrue(message, condition)

  def assertTrue(condition: Boolean) = Assert.assertTrue(condition)

  def assertFalse(message: String, condition: Boolean) = Assert.assertFalse(message, condition)

  def assertFalse(condition: Boolean) = Assert.assertFalse(condition)

  def fail(message: String) = Assert.fail(message)

  def fail() = Assert.fail()

  def assertEquals(message: String, expected: AnyRef, actual: AnyRef) = Assert.assertEquals(message, expected, actual)

  def assertEquals(expected: AnyRef, actual: AnyRef) = Assert.assertEquals(expected, actual)

  def assertEquals(message: String, expected: Double, actual: Double, delta: Double)
      = Assert.assertEquals(message, expected, actual, delta)

  def assertEquals(expected: Long, actual: Long) = Assert.assertEquals(expected, actual)

  def assertEquals(message: String, expected: Long, actual: Long) = Assert.assertEquals(message, expected, actual)

  def assertEquals(expected: Float, actual: Float, delta: Float) = Assert.assertEquals(expected, actual, delta)

  def assertEquals(expected: Double, actual: Double, delta: Double) = Assert.assertEquals(expected, actual, delta)

  def assertNotNull(message: String, obj: AnyRef) = Assert.assertNotNull(message, obj)

  def assertNotNull(obj: AnyRef) = Assert.assertNotNull(obj)

  def assertNull(message: String, obj: AnyRef) = Assert.assertNotNull(message, obj)

  def assertNull(obj: AnyRef) = Assert.assertNull(obj)

  def assertSame(message: String, expected: AnyRef, actual: AnyRef) = Assert.assertSame(message, expected, actual)

  def assertSame(expected: AnyRef, actual: AnyRef) = Assert.assertSame(expected, actual)

  def assertNotSame(message: String, unexpected: AnyRef, actual: AnyRef) = Assert.assertNotSame(message, unexpected, actual)

  def assertNotSame(unexpected: AnyRef, actual: AnyRef) = Assert.assertNotSame(unexpected, actual)
}
