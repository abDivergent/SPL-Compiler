import lexer.cLexer;
import lexer.cLinkedList;
import parser.cParser;
import parser.cTreeNode;

public class Main
{
    public static void main(String[] args)
    {
        cLexer oLexer = new cLexer("./src/test.txt");
        cParser parser;
        cLinkedList oList = null;
        cTreeNode tree;
        try
        {
            oList = oLexer.start();
            System.out.println("Lexing complete");

            parser = new cParser(oList);
            tree = parser.start();
            System.out.println("Parsing complete complete");

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
