import Lexer.cLexer;
import Lexer.cLinkedList;

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
            return;
        }

        System.out.print(oList.toString());
        return;
    }
}
