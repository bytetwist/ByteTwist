public class NodeForRenaming {

    public final String s;

    public NodeForRenaming(String s)
    {
        this.s = s;
        renameMe(s);
    }

    public String renameMe(String string)
    {
        return s;
    }

    public NodeForRenaming paramsTest(NodeForRenaming nodeForRenaming)
    {
        renameMe(s);
        // Make sure param types get renamed when renaming class
        return this;
    }
}
