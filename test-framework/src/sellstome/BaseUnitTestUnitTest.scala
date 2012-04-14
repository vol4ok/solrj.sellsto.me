package sellstome

/**
 * :=)
 * @author Aliaksandr Zhuhrou
 * @since 1.0
 */
class BaseUnitTestUnitTest extends BaseUnitTest {

  test("test array equal") {
    val arrFloat1  = Array(1.0f, 0.0001f, 345.34f, 0.3442f)
    val arrFloat2  = Array(1.0f, 0.0001f, 345.34f, 0.3442f)
    val arrDouble1 = Array(1.0d, 0.0001d, 345.34d, 0.3442d)
    val arrDouble2 = Array(1.0d, 0.0001d, 345.34d, 0.3442d)
    assertArrayEqual(arrFloat1,  arrFloat2)
    assertArrayEqual(arrDouble1, arrDouble2)
  }

}