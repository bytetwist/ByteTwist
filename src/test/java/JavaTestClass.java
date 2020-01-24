public class JavaTestClass implements JavaTestInterface
{

    public static int testField1 = 0;

    public int testField2 = 1000;

    public void testMethod1()
    {
        if (testField1 == 1)
        {
            testMethod2();
        }
        else
        {
            testMethod2();
        }
        testField1 = 1000;
        int i = testField1 * testField2;
    }

    public void testMethod2()
    {
        JavaTestClass javaTestClass = new JavaTestClass();
        int i = javaTestClass.testField2;
    }

    public void testMethod3()
    {
        for (int i = 0; i < 10; i++)
        {
            if (testField1 == 10000)
            {
                //
            }
        }
    }
}
