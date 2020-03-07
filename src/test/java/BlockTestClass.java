public class BlockTestClass {

    public void methodWith5Blocks()
    {
        int i = 0;
        i++;
        if (i == (Math.addExact(i, 4)))
        {
            i++;
        }
        if (i == Math.incrementExact(2)){
            i--;
        }
        else
        {
            int abs = Math.abs(i);
        }
        if (i == 0) {
            return;
        }

    }

    public void tryCatchBlock()
    {
        try
        {
            System.out.println("try block");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
