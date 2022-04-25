package lexer;

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

    public cLinkedList getList()
    {
        return oList;
    }

    public cLinkedList start() throws Exception
    {
        String message;
        if(m_sFilePath.trim().isEmpty())
        {
            throw new Exception("No File path Specified");
        }

        File oFile = new File(m_sFilePath);
        FileReader oFileReader = new FileReader(oFile);
        BufferedReader oBufferReader = new BufferedReader(oFileReader);

        int iNextChar;

        while ((iNextChar = oBufferReader.read()) != -1)
        {
            char c = (char) iNextChar;

            // Check if valid Char
            if(!isValidChar(c))
            {
                message = "[Lexical Error] "+ c +"(ascii: "+(int) c+") Invalid Character. Scanning aborted";
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
                else if ( (c >= '0') && (c <= '9') ||  (c == '-') )
                {
                    // Number
                    // System.out.println("+ Number");
                    readNumber(oBufferReader, c);
                }
                else if(!isValidWhiteSpace(c))
                {
                    if(isGroupingSymbol(c))
                    {
                        insertGroupingSymbol(c);
                    }
                    else if (c ==':')
                    {
                        insertAssignmentSymbol(c, oBufferReader);
                    }
                    else {
                        message = "[Lexical Error] "+ c +"(ascii "+iNextChar+") Unidentified error. Scanning aborted";
                        oList.add(message, eNodeType.Error);
                        throw new Exception(message);
                    }
                }

            }


        }
        return oList;
    }

    private void readNumbers(BufferedReader oReader, char c) throws Exception
    {
        int iNextChar = 0;
        String message;
        StringBuilder sStringBuilder;

        if(c == '-' && ((iNextChar = oReader.read()) == -1 || '0'==(char) iNextChar))
        {
            // if the first character is '-' and the following is null, zero or a non number character
            message = "[Lexical Error] at "+c+(char)iNextChar+" (unexpected char after -).";
            oList.add(new cNode(message, eNodeType.Error));
            throw new Exception(message);
        }
        else
        {
            sStringBuilder = new StringBuilder(String.valueOf(c));
            do
            {
                c = (char) iNextChar;
                if(isNumber(c))
                {
                    sStringBuilder.append(c);
                }
                else if(c =='.')
                {
                    if(sStringBuilder.toString().contains("."))
                    {
                        message = "[Lexical Error]  Unexpected token "+ c +"(ascii "+iNextChar+"). Scanning aborted";
                        oList.add(new cNode(message, eNodeType.Error));
                        throw new Exception(message);
                    }
                    else
                    {
                        sStringBuilder.append(c);
                    }
                }
                else if( isValidWhiteSpace(c))
                {
                    oList.add(new cNode(sStringBuilder.toString(), eNodeType.Number));
                    return;
                }
                else if( isValidChar(c))
                {
                    message = "[Lexical Error] at "+sStringBuilder+c+" ["+c+" (ascii "+(int)c+") is not a number].";
                    oList.add(new cNode(message, eNodeType.Error));
                    throw new Exception(message);
                }
                else
                {
                    message = "[Lexical Error] at "+sStringBuilder+c+" ["+c+" (ascii "+(int)c+") is not valid].";
                    oList.add(new cNode(message, eNodeType.Error));
                    throw new Exception(message);
                }
            }while ((iNextChar = oReader.read()) != -1);

            oList.add(sStringBuilder.toString(), eNodeType.Number);
        }

    }

    private void readNumber(BufferedReader oReader, char c) throws Exception
    {
        int iNextChar;
        String message;
        StringBuilder sNumber;

        if((iNextChar = oReader.read()) != -1)
        {
            if (c == '-' && (!isNumber((char) iNextChar) || ('0' == (char) iNextChar)))
            {
                // '-' can only be followed by a non-zero number
                // Error if iNextChar is anything other than a non-zero umber
                message = "[Lexical Error] at " + c + (char) iNextChar + " (unexpected char after -).";
                oList.add(new cNode(message, eNodeType.Error));
                throw new Exception(message);
            }
            else
            {
                sNumber = new StringBuilder(String.valueOf(c));
            }
            do
            {
                c = (char) iNextChar;
                if(isNumber(c) || c=='.')
                {
                    // if c== '-' this should always run on the first iteration
                    sNumber.append(c);
                }
                else if(isValidWhiteSpace(c))
                {
                    // white space after number
                    // will never run on the first iteration because of second 'if', when c == '-'
                    oList.add(new cNode(sNumber.toString(), eNodeType.Number));
                    return;
                }
                else if(isGroupingSymbol(c))
                {
                    // grouping symbol after number
                    // will never run on the first iteration because of second 'if', when c == '-'
                    oList.add(new cNode(sNumber.toString(), eNodeType.Number));
                    insertGroupingSymbol(c);
                    return;
                }
                else
                {
                    // Any other character besides a number, white space or grouping symbol is not valid
                    message = "[Lexical Error] Unexpected token "+c+" (ascii "+(int)c+").";
                    oList.add(new cNode(message, eNodeType.Error));
                    throw new Exception(message);
                }
            } while (((iNextChar = oReader.read()) != -1));
        }
        else
        {
            if (c == '-')
            {
                // c== '-' and no number follows after
                message = "[Lexical Error] Unexpected end of string after -";
                oList.add(new cNode(message, eNodeType.Error));
                throw new Exception(message);
            }
        }
        // Stops when:
        // 1. while loop condition is false
        // 2. first 'if' is false and c != '-'
        oList.add(new cNode(String.valueOf(c), eNodeType.Number));
    }

    private void readKeywordOrUDN(BufferedReader oReader, char c) throws Exception
    {
        StringBuilder sStringBuilder = new StringBuilder(String.valueOf(c));
        String message;
        int iNextChar;
        while(((iNextChar = oReader.read()) != -1) && isUDN((char) iNextChar))
        {
            c = (char) iNextChar;
            sStringBuilder.append(c);
        }

        if(!isKeyword(String.valueOf(sStringBuilder)))
        {
            // isKeyword Function automatically adds a new node to list if the string is a keyword
            // else it needs to be manually added as follows
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
                insertAssignmentSymbol(c, oReader);
            }
            else if(c == '-')
            {
                readNumber(oReader, c);
            }
            else if(!isValidWhiteSpace(c))
            {
                message = "[Lexical Error]Unexpected Error: "+c+" (ascii "+(int) c +")";
                oList.add(message, eNodeType.Error);
                throw new Exception(message);
            }
        }
    }

    private void insertAssignmentSymbol(char c, BufferedReader oReader) throws Exception
    {
        int iNextChar;
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
                oList.add("[Lexical Error ]"+c+ (char)iNextChar+" (ascii "+iNextChar+"+). Expected =",
                        eNodeType.Error);
            }
        }
        else
        {
            // Invalid character after :
            // Was supposed to be an assignment symbol
            oList.add("[Lexical Error] Unexpected end of program after "+c, eNodeType.Error);
        }
    }

    private void shortString(BufferedReader oReader, char c) throws Exception
    {
        StringBuilder sShortString = new StringBuilder(String.valueOf(c));

        int iStringLength = 0;
        int iNextChar;
        String message;
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
                message = "[Lexical Error] "+ c +"(ascii: "+(int) c+"). Invalid Character. Scanning aborted";
                oList.add(new cNode(message
                        , eNodeType.Error));
                throw new Exception(message);
            }
            else
            {
                message = "[Lexical Error] "+sShortString+ c +". Invalid string name. Scanning aborted.";
                oList.add(new cNode(message,
                        eNodeType.Error));
                throw new Exception(message);
            }
        }
        message = "[Lexical Error] "+sShortString+". string too long. scanning aborted";
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
                String message = "[Lexical Error] Expected Grouping symbol in place of "+c+" (ascii "+(int)c+").";
                oList.add(message, eNodeType.Error);
                throw new Exception(message);
        }
    }

    private boolean isShortStringChar(char c)
    {
        return ((c == ' ' || isNumber(c) || ((c >= 'A') && (c <= 'Z')) ));
    }

    private boolean isUDN(char c)
    {
        return (isNumber(c) || ((c >= 'a') && (c <= 'z')) );
    }

    private boolean isValidChar(char c)
    {
        if( isNumber(c)) return true;

        if((c >= 'A') && (c <= 'A')) return true;

        if((c >= 'a') && (c <= 'z')) return true;

        // Carriage return or space
        if (isValidWhiteSpace(c)) return true;

        char[] oCharArray = {'{','}',';',',', ':', '=','(',')','[',']','-','"'};
        for (char value : oCharArray)
        {
            if (c == value) return true;
        }
        return false;
    }

    private boolean isKeyword(String sUDN)
    {
        String[] boolOperators = {"true", "false", "not", "and", "or"};
        String[] compOperator = {"eq", "larger"};
        String[] numOperators = {"add", "sub", "mult"};
        String[] types ={"num", "bool", "string","arr"};
        String[] ioCommands = {"input", "output"};
        String[] keywords = { "main", "return", "if", "then", "else", "do", "until", "while","call"};


        if(sUDN.equals("halt"))
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.SpecialCommand));

        else if( stringWithinList( compOperator, sUDN ) )
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.Comparison));

        else if( stringWithinList( ioCommands, sUDN ))
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.IOCommand));

        else if( sUDN.equals("proc") )
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.PDKeyword));

        else if( stringWithinList( boolOperators, sUDN ) )
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.BooleanOp));

        else if( stringWithinList( numOperators, sUDN ) )
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.NumberOp));
        else if( stringWithinList( types, sUDN ))
            oList.add(new cNode(sUDN, eNodeType.Keyword, eNodeSubType.VarType));

        else if( stringWithinList(keywords, sUDN))
            oList.add(new cNode(sUDN, eNodeType.Keyword));

        else
            return false;
        return true;
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

    private boolean stringWithinList(String[] pList, String pString)
    {
        for (String word : pList)
        {
            if(word.equals(pString))
                return true;
        }
        return false;
    }

    private boolean isNumber(char c)
    {
        return '0' <= c && c <= '9';
    }

    private boolean isValidWhiteSpace(char c)
    {
        return ((int) c == 10)|| ((int)c == 13) || ((int)c == 32);
    }
}
