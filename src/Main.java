
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
                    System.out.println("Lexing...");
                    cLinkedList oList = oLexer.start();

                    cParser parser = new cParser(oList);
                    System.out.println("Parsing...");
                    parser.start();

                    String treeString = parser.printTree();
//                    System.out.println("complete\n results in "+treeString);
                    writeToFile(treeString, count++);
                }
            } catch (Exception e)
            {
                System.out.println("Error found");
                writeToFile(e.getMessage(), count++);
            }
        }
/*        boolean exit = false;
        String directory = "";
        Scanner scanner = new Scanner(System.in);
        int count = 0;


        while (!exit)
        {
            try
            {
                System.out.println("1.Enter file directory or Enter exit to terminate program");
//                directory = scanner.nextLine();
                directory = "src/test.txt";
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
                    parser.start();

                    String treeString = parser.printTree();
                    System.out.println("complete\n results in "+treeString);
                    exit = true;
                }


            } catch (Exception e)
            {
                writeToFile(directory, count++ );
            }
            writeToFile(directory, count++ );

        }*/


        ///////////////////////////////////////////////////////////////////////////////
        //todo delete this method and use the "Shrey" method above
/*
        int i = 0;
        boolean stop = false;
        while (!stop && i < 13)
        {
            i++;
            stop = true;
            try{
                cLexer oLexer = new cLexer("src/test/lexerTests/invalid"+i+".txt");
                System.out.println("pass "+i);
                cLinkedList oList = oLexer.start();
//                System.out.println("Error found brooooooooooooooooooooooooooo");

//                cParser parser = new cParser(oList);
//                System.out.println("Parsing...");
//                parser.start();

//                String treeString = parser.printTree();
//                System.out.println("complete\n results in "+treeString);
            } catch (Exception e)
            {

                System.out.println(e.getMessage());
//                System.out.println("\n\n continue?");
//                if(!scanner.nextLine().equals("exit"))
                    stop = false;

            }
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
