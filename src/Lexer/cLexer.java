package Lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class cLexer
{
    private final cLinkedList oList;
    private final String m_sFilePath;
    public cLexer(String pFilePath)
    {
        oList = new cLinkedList();
        m_sFilePath = pFilePath;
    }

    public cLexer()
    {
        oList = new cLinkedList();
        m_sFilePath = "";
    }

    public cLinkedList getList()
    {
        return oList;
    }

    public cLinkedList start() throws Exception
    {
        String message = "";
        if(m_sFilePath.trim().isEmpty())
        {
            throw new Exception("No File path Specified");
        }

        File oFile = new File(m_sFilePath);
        FileReader oFileReader = new FileReader(oFile);
        BufferedReader oBufferReader = new BufferedReader(oFileReader);

        int data;

        while ((data = oBufferReader.read()) != -1)
        {
            char c = (char) data;

            // Check if valid Char
            if(!isValidChar(c))
            {
                message = "Lexical Error: "+ c +"(ascii: "+(int) c+") Invalid Character. Scanning aborted";
                oList.add(new cNode( message, eNodeType.Error));
                throw new Exception(message);
            }else
            {
                if (c == '"')
                {
                    // Short String
                    // System.out.println("+ short String");
                    shortString(oBufferReader, c);
                }
                else if( (c >= 'a') && (c <= 'z'))
                {
                    // UDN or keyword
                    // System.out.println("+ UDN/Keyword");
                    readKeywordOrUDN(oBufferReader, c);
                }
            }


        }
        return oList;
    }

    private void readKeywordOrUDN(BufferedReader oReader, char c) throws Exception
    {
        StringBuilder sStringBuilder = new StringBuilder(String.valueOf(c));
        String message ="";
        int iNextChar;
        while(((iNextChar = oReader.read()) != -1) && isUDN((char) iNextChar))
        {
            c = (char) iNextChar;
            sStringBuilder.append(c);
        }
        eNodeType type = eNodeType.UserDefinedName;
        if(isKeyword(String.valueOf(sStringBuilder)))
        {
            oList.add(String.valueOf(sStringBuilder), eNodeType.Keyword);
        }
        else
        {

            oList.add(String.valueOf(sStringBuilder), eNodeType.UserDefinedName);
        }
        if(c != (char) iNextChar)
        {
            c = (char) iNextChar;
            if(isGroupingSymbol(c))
            {
                insertGroupingSymbol(c);
            }
            else if (c == ':')
            {
                // Reading Assignment Symbol
                if((iNextChar = oReader.read()) != -1)
                {
                    if((char)iNextChar == '=')
                    {
                        // Assignment symbol is correct (:=)
                        oList.add(":=", eNodeType.Assignment);
                    }
                    else
                    {
                        // Invalid character after :
                        // Was supposed to be an assignment symbol
                        oList.add("Lexical Error: "+c+ (char)iNextChar+" (ascii "+iNextChar+"+). Expected =.",
                                eNodeType.Error);
                    }
                }
                else
                {
                    // Invalid character after :
                    // Was supposed to be an assignment symbol
                    oList.add("Lexical Error: Unexpected end of program after "+c, eNodeType.Error);
                }
            }
            else if(!(c == (char)13)&& !(c == (char) 32))
            {
                message = "Unexpected Error: "+c+" (ascii "+(int) c +")";
                oList.add(message, eNodeType.Error);
                throw new Exception(message);
            }
        }
        System.out.println("Lexing complete.");
        System.out.println(c+"(ascii "+(int)c+ ")");
    }

    private void shortString(BufferedReader oReader, char c) throws Exception
    {
        StringBuilder sShortString = new StringBuilder(String.valueOf(c));

        int iStringLength = 0;
        int iNextChar;
        String message = "";
        while ((iStringLength++ <= 15) && ((iNextChar = oReader.read()) != -1))
        {
            c = (char)iNextChar;
            if( isShortStringChar(c))
            {
                sShortString.append(c);

            }
            else if (c == '"')
            {
                sShortString.append(c);
                oList.add(new cNode(sShortString.toString(), eNodeType.ShortString));
                return;
            }
            else if (!isValidChar(c))
            {
                message = "Lexical Error: "+ c +"(ascii: "+(int) c+"). Invalid Character. Scanning aborted";
                oList.add(new cNode(message
                        , eNodeType.Error));
                throw new Exception(message);
            }
            else
            {
                message = "Lexical Error: "+sShortString+ c +". Invalid string name. Scanning aborted.";
                oList.add(new cNode(message,
                        eNodeType.Error));
                throw new Exception(message);
            }
        }
        message = "Lexical Error: "+sShortString+". string too long. scanning aborted";
        oList.add(new cNode(message, eNodeType.Error));
        throw new Exception(message);
    }

    private void insertGroupingSymbol(char c) throws Exception
    {
        switch (c)
        {
            case '[':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.LeftSquareBracket);
                break;
            case ']':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.RightSquareBracket);
                break;
            case'{':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.LeftCurlyBrace);
                break;
            case'}':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.RightCurlyBrace);
                break;
            case'(':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.LeftBracket);
                break;
            case')':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.RightBracket);
                break;
            case';':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.SemiColon);
                break;
            case',':
                oList.add(String.valueOf(c), eNodeType.Grouping, eNodeSubType.Comma);
                break;
            default:
                String message = "Lexical Error: Expected Grouping symbol in place of "+c+" (ascii "+(int)c+").";
                oList.add(message, eNodeType.Error);
                throw new Exception(message);
        }
    }

    private boolean isShortStringChar(char c)
    {
        return ((c == ' ' || ((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'Z')) ));
    }

    private boolean isUDN(char c)
    {
        return (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) );
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

    private boolean isKeyword(String sUDN)
    {
        String[] keywords = {"halt", "proc","main", "return", "if", "then", "else", "do", "until", "while", "output",
                "input","call","true","false","not", "and", "or", "eq","larger","add", "sub","mult","arr","num","bool",
                "string"};
        for (String word:keywords)
        {
            if(word.trim().equals(sUDN)) return true;
        }
        return false;
    }

    private boolean isGroupingSymbol(char c)
    {
        char[] symbols = {';',',','(',')','[',']','{','}'};
        for (char symbol: symbols)
        {
            if(symbol == c) return true;
        }
        return false;
    }
}
