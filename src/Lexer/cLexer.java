package Lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

public class cLexer
{
    private final LinkedList<cNode> oList;
    private final String m_sFilePath;
    public cLexer(String pFilePath)
    {
        oList = new LinkedList<>();
        m_sFilePath = pFilePath;
    }

    public cLexer()
    {
        oList = new LinkedList<>();
        m_sFilePath = "";
    }

    public LinkedList<cNode> start() throws Exception
    {
        if(m_sFilePath.trim().isEmpty())
        {
            throw new Exception("No File path Specified");
        }

        File oFile = new File(m_sFilePath);
        FileReader oFileResder = new FileReader(oFile);
        BufferedReader oBufferResder = new BufferedReader(oFileResder);

        int data;

        try
        {
            while ((data = oBufferResder.read()) != -1)
            {
                char c = (char) data;

                // Check if valid Char
                if(!isValidChar(c))
                {
                    oList.add(new cNode("Lexical Error: "+ c +"(ascii: "+(int) c+") Invalid Character. Scanning aborted"));
                    throw new Exception("Lexical Error: "+ c +"(ascii: "+(int) c+")");
                }else
                {
                    if (c == '"')
                    {
                        //Short String
                        System.out.println("+ short String");

                    }
                }


            }
        } finally
        {

            return oList;
        }
    }

    private boolean isValidChar(char c)
    {
        if( isShortStringChar(c)) return true;

        if((c >= 'a') && (c <= 'z')) return true;

        if (((int) c == 10)|| ((int)c == 13)) return true;

        char[] oCharArray = {'{','}',';',',', ':', '=','(',')','[',']','-','"'};
        for (char value : oCharArray)
        {
            if (c == value) return true;
        }
        return false;
    }

    private void shortString(BufferedReader oReader, char c) throws Exception
    {
        String sShortString = String.valueOf(c);

        int iStringLength = 0;
        int iNextChar;
        while ((iStringLength++ < 15) && ((iNextChar = oReader.read()) != -1))
        {
            c = (char)iNextChar;
            if( isShortStringChar(c))
            {

            }else
            {
                if(c == '"')
                {
                    sShortString += c;
                    oList.add(new cNode(sShortString, eNodeType.ShortString));
                }
                break;
            }
        }
        if(isValidChar(c))
        {
          if(iStringLength >=16)
            {
                oList.add(new cNode("Lexical Error: "+sShortString+c+" = String too long. Scanning aborted"));
                throw new Exception("Lexical Error: "+sShortString+c);
            }

        }else
        {
            oList.add(new cNode("Lexical Error: "+ c +"(ascii: "+(int) c+") Invalid Character. Scanning aborted"));
            throw new Exception("Lexical Error: "+ c +"(ascii: "+(int) c+")");
        }
    }

    private boolean isShortStringChar(char c)
    {
        return ((c == ' ' || ((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'Z')) ));
    }
}
