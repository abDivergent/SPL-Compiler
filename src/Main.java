
import java.io.File;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        ///////////////////////////////////////////////////////////////////////////////
        //todo                                 complete Shrey's jar method
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
                System.out.println("Error found brooooooooooooooooooooooooooo");

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
        }
    }

    private static void writeToFile(String filename, int i)
    {
        File input = new File(filename);
        System.out.println("Name: "+input.getName());
        System.out.println("Path: "+input.getPath());
        System.out.println("Absolute Path: "+input.getAbsolutePath());
        System.out.println("Parent: "+input.getParent());
    }

}
