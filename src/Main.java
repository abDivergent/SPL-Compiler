
import Lexer.cLexer;
import Lexer.cLinkedList;
import Sementics.Naming.TypeChecking;
import Sementics.Naming.VariableAnalysis;
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

        while (!exit)
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
                    cLinkedList oList = oLexer.start();

                    cParser parser = new cParser(oList);
                    cTreeNode tree = parser.start();

                    Scoping scoping = new Scoping(tree);
                    tree =  scoping.start();

                    VariableAnalysis varAnalysis = new VariableAnalysis(tree);
                    cTreeNode namedTree = varAnalysis.start();

                    cTreeNode prunedTree = parser.pruneTree(namedTree);

                    TypeChecking tc = new TypeChecking(prunedTree);
                    cTreeNode typeChecked = tc.start();

                    String treeString = tc.printTree(typeChecked);

                    File file = new File(directory);
                    if(file.getName().contains("invalid") || file.getName().contains("error"))
                        throw new Exception("[Unexpected Error] an unexpected error has occurred");

                    writeToFile(treeString, count++);

                }
            } catch (Exception e)
            {
                System.out.println("Error found");
                writeToFile(e.getMessage(), count++);
            }
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*        try
        {
            TestFileCreator testCreator = new TestFileCreator();
            testCreator.createToFile("src/test.txt");

            cLexer oLexer = new cLexer("src/test/naming/invalid16.txt");
            cLinkedList oList = oLexer.start();

            cParser parser = new cParser(oList);
            System.out.println("Parsing...");
            cTreeNode parsedTree = parser.start();
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

            cTreeNode prunedTree = parser.pruneTree(namedTree);
            treeString = varAnalysis.printTree(prunedTree);
            writeToFile(treeString, count++);

            TypeChecking tc = new TypeChecking(prunedTree);
            System.out.println("type checking...");
            cTreeNode typeChecked = tc.start();
            treeString = tc.printTree(typeChecked);
            writeToFile(treeString, count++);
        }
        catch (Exception e)
        {
            System.out.println("Error found");
            if(e.getMessage() == null)
                e.printStackTrace();
            else
                writeToFile(e.getMessage(), count++);
        }*/
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
