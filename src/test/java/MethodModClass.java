public class MethodModClass
{

    private int field1 = 0;

    public void testModMethod()
    {
        if (field1 == 0)
        {
            field1 = 1;
        }
    }

    public void tryCatch()
    {
        try
        {
            System.out.println("trying");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
