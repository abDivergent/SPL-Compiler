
import Lexer.cLexer;
import Lexer.cLinkedList;
import Naming.VariableAnalysis;
import Node.cTreeNode;
import Parser.cParser;
import Sementics.Scoping;
import test.TestFileCreator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;
        String directory = "";
        int count = 1;

/*        while (!exit)
        {
            try
            {
                System.out.println("1.Enter file directory or Enter exit to terminate program");
                directory = scanner.nextLine();

                if(directory.trim().equals(""))
                    System.out.println("1.Enter file directory or Enter exit to terminate program");
                else if (directory.trim().equalsIgnoreCase("exit"))
                {
                    System.out.println("Terminating program");
                    exit = true;
                }
                else
                {

                    cLexer oLexer = new cLexer(directory);
                    System.out.println("Lexing...");
                    cLinkedList oList = oLexer.start();

                    cParser parser = new cParser(oList);
                    System.out.println("Parsing...");
                    cTreeNode tree = parser.start();
                    Scoping scoping = new Scoping(tree);
                    scoping.start();

//                    String treeString = parser.printTree();
                    String treeString = scoping.printTree();
//                    System.out.println("complete\n results in "+treeString);
                    writeToFile(treeString, count++);
                }
            } catch (Exception e)
            {
                System.out.println("Error found");
                writeToFile(e.getMessage(), count++);
            }
        }*/

        try
        {
//            TestFileCreator testCeator = new TestFileCreator();
//            testCeator.createToFile("src/test.txt");

            cLexer oLexer = new cLexer("src/test/naming/valid9.txt");
            System.out.println("Lexing...");
            cLinkedList oList = oLexer.start();

            cParser parser = new cParser(oList);
            System.out.println("Parsing...");
            cTreeNode parsedTree = parser.start(true, false);
            String treeString = parser.printTree();
            writeToFile(treeString, count++);

            Scoping scoping = new Scoping(parsedTree);
            System.out.println("Scoping...");
            cTreeNode scopedTree = scoping.start();
            treeString = scoping.printTree();
            writeToFile(treeString, count++);

            VariableAnalysis varAnalysis = new VariableAnalysis(scopedTree);
            System.out.println("naming...");
            cTreeNode namedTree = varAnalysis.start();
            treeString = varAnalysis.printTree();
            writeToFile(treeString, count++);
        }
        catch (Exception e)
        {
            System.out.println("Error found");
            if(e.getMessage() == null)
                e.printStackTrace();
            else
                writeToFile(e.getMessage(), count++);
        }
    }

    private static void writeToFile(String str, int i)
    {
        try
        {
            String filepath = System.getProperty("user.dir")+"\\results"+i+".txt";
            File resultFile = new File(filepath);
            resultFile.createNewFile();

            FileWriter myWriter = new FileWriter(filepath, false);
            myWriter.write(str);
            myWriter.close();
            System.out.println("results saved to "+filepath);
        } catch (IOException e)
        {
            System.out.println("An error occurred when writing to file.");
//            e.printStackTrace();
        }
    }

}
