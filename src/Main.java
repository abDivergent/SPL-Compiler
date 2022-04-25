import lexer.cLexer;
import lexer.cLinkedList;

public class Main
{
    public static void main(String[] args)
    {
        cLexer oLexer = new cLexer("./src/test.txt");
        cLinkedList oList;
        try
        {
            oList = oLexer.start();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("//////////////////////////////////////////////////////////////////////////////");
            System.out.println(oLexer.getList().toString());
            return;
        }
        System.out.println("////////////////////////Lexing Complete");
        System.out.print(oList.toString());
        return;
    }
}
