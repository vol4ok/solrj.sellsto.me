package common;

/**
 * @author Aliaksandr Zhuhrou
 */
public class JavaLanguageUnitTest {

    public static void main(String[] args) {
        String testStr = "one";
        Object testObj = testStr;
        JavaLanguageUnitTest test = new JavaLanguageUnitTest();
        test.print(testStr);
        test.print(testObj);
    }

    public void print(String test) {
        System.out.println("String: " + test);
    }

    public void print(Object test) {
        System.out.println("Object: "+test);
    }

}
