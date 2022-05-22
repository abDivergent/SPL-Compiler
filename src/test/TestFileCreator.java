package test;

import Node.cNode;
import Node.eNodeSubType;
import Node.eNodeType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestFileCreator
{
    private int cProcDefs = 5;
    private int cAlg = 7;
    private int cAlt = 7;
    private int cVarDec = 7;
    private final Random rand = new Random();

    public TestFileCreator()
    {
    }

    /**
     * The following parameters determine the maximum number of times a production
     * can be used when generating code.
     *
     * This does not stop the code from generating productions less than the
     * max since productions are chosen at random
     *
     * @param cProcDef maximum number of procDefs productions to generate
     * @param cAlg maximum number of  Algorithm productions to generate
     * @param cAlt maximum number of Alternat productions to generate
     * @param cVarDec maximum number of VarDecl productions to generate
     */
    public TestFileCreator(int cProcDef, int cAlg, int cAlt, int cVarDec)
    {
        this.cProcDefs = cProcDef;
        this.cAlg = cAlg;
        this.cAlt = cAlt;
        this.cVarDec = cVarDec;
    }

    /**
     * Creates a valid SPL string
     * @return valid SPL string
     */
    public String create()
    {
        return spl();
    }

    public void createToFile(String path) throws IOException
    {
        writeToFile(spl(), path);
    }

    private String spl()
    {
        return procDefs() + " main { " + algorithm() + " halt ; " + varDecl() + " } ";
    }

    private String varDecl()
    {
        int r;
        r = rand.nextInt(2);
        if (cVarDec > 0)
            cVarDec--;
        else
            r = 0;

        if (r == 1)
            return dec() + " ; " + varDecl();
        else
            return " ";
    }

    private String dec()
    {
        if (rand.nextBoolean())
            return typ() + " " + var();
        else
            return "arr " + typ() + " [ " + Const() + " ] " + var();
    }

    private String Const()
    {
        int r = rand.nextInt(4);

        if (r == 0)
            return genShortString(rand.nextInt(15) + 1);
        else if (r == 1)
            return String.valueOf(rand.nextInt());
        else if (r == 2)
            return " true ";
        else
            return " false ";
    }

    public String genShortString(int i)
    {
        StringBuilder str = new StringBuilder(" \"");
        while (i > 0)
        {
            str.append(genChar().toUpperCase());
            i--;
        }
        str.append("\" ");
        return str.toString();
    }

    private String genChar()
    {
        return String.valueOf((char) (rand.nextInt(90 - 64) + 65));
    }

    private String var()
    {

        return udn(rand.nextInt(8) + 1);
    }

    public String udn(int i)
    {
        StringBuilder str = new StringBuilder(genChar().toLowerCase());
        while (i > 0)
        {
            str.append(genChar().toLowerCase());
            i--;
        }
        if (isKeyword(String.valueOf(str)))
            return udn(i);
        return " " + str + " ";

    }

    private String typ()
    {
        int r = rand.nextInt(3);

        if (r == 0)
            return " num ";
        if (r == 1)
            return " bool ";
        else
            return " string ";
    }

    private String algorithm()
    {
        int r;
        r = rand.nextInt(2);
        if (cAlg > 0)
            cAlg--;
        else
            r = 0;

        if (r == 1)
            return instr() + " ; " + algorithm();
        else
            return "";
    }

    private String instr()
    {
        switch (rand.nextInt(4))
        {
            case 0:
                return assign();
            case 1:
                return branch();
            case 2:
                return loop();
            default:
                return pcall();
        }
    }

    private String pcall()
    {
        return " call " + udn(rand.nextInt(8) + 1);
    }

    private String loop()
    {
        if (rand.nextBoolean())
            return " do { " + algorithm() + " } until ( " + expr(4) + " ) ";
        else
            return " while ( " + expr(3) + ") do { " + algorithm() + " } ";
    }

    /**
     * since some productions of Expr are indirectly recursive, the variable
     * i will restrict the recursion from going deeper than i times.
     *
     * i > 1 means BinOp production can be called
     * i > 0 means UnOp production can be called
     * @param i max number of Expr that can be produced
     * @return expression string
     */
    private String expr(int i)
    {
        int r;
        if (i > 1)
            r = rand.nextInt(5);
        else if (i > 0)
            r = rand.nextInt(4);
        else
            r = rand.nextInt(3);

        switch (r)
        {
            case 0:
                return Const();
            case 1:
                return var();
            case 2:
                return field();
            case 3:
                return unop((i - 1));
            default:
                return binop((i - 2));
        }
    }

    /**
     * BinOp passes the @i counter to its Expr production
     *
     * @param i max number of Expr production
     * @return binary operator expression string
     */
    private String binop(int i)
    {
        switch (rand.nextInt(7))
        {
            case 0:
                return " and( " + expr(i) + "," + expr(i) + " )";
            case 1:
                return " or( " + expr(i) + "," + expr(i) + " )";
            case 2:
                return " eq( " + expr(i) + "," + expr(i) + " )";
            case 3:
                return " larger( " + expr(i) + "," + expr(i) + " )";
            case 4:
                return " add( " + expr(i) + "," + expr(i) + " )";
            case 5:
                return " sub( " + expr(i) + "," + expr(i) + " )";
            default:
                return " mult( " + expr(i) + "," + expr(i) + " )";
        }
    }

    /**
     * UnOp passes the @i counter to its Expr production
     *
     * @param i max number of Expr production
     * @return unary operator expression string
     */
    private String unop(int i)
    {
        if (rand.nextBoolean())
            return " input(" + var() + ") ";
        else
            return " not(" + expr(i) + ") ";

    }

    private String field()
    {
        String str = udn(rand.nextInt(8) + 1) + "[";
        if (rand.nextBoolean())
            str += Const();
        else
            str += var();

        return str + "]";
    }

    private String branch()
    {
        return "if (" + expr(3) + ") then { " + algorithm() + " } " + alt();
    }

    private String alt()
    {
        if (cAlt-- > 0)
            return " else { " + algorithm() + " }";
        else
            return "";
    }

    private String assign()
    {
        return lhs() + " := " + expr(3);
    }

    private String lhs()
    {
        switch (rand.nextInt(3))
        {
            case 0:
                return "output";
            case 1:
                return var();
            default:
                return field();
        }
    }

    private String procDefs()
    {
        int r;
        r = rand.nextInt(2);
        if (cProcDefs > 0)
            cProcDefs--;
        else
            r = 0;

        if (r == 1)
            return pd() + "," + procDefs();
        else
            return "";
    }

    private String pd()
    {
        return " proc " + udn(
                rand.nextInt(8) + 1) + " { " + procDefs() + " " + algorithm() + " return ; " + varDecl() + " } ";
    }

    /**
     * writes given string into an existing given file
     *  @param str string to be written ro fil
     * @param file path to existing file
     */
    public void writeToFile(String str, String file) throws IOException
    {
        try
        {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(str);
            myWriter.close();
        } catch (IOException e)
        {
            System.out.println("An error occurred.");
            throw e;
        }
    }

    private boolean isKeyword(String sUDN)
    {
        String[] keywords1 = {"eq", "larger","input", "output", "num", "bool", "string","arr","add", "sub", "mult"};
        String[] keywords2 = {"true", "false", "not", "and", "or","main", "return", "if", "then", "else", "do",
                "until", "while","call"};
        for (String str: keywords1)
        {
            if( str.equals(sUDN))
                return true;
        }
        for (String str : keywords2)
        {
            if (str.equals(sUDN))
                return true;
        }
        return false;
    }

}
