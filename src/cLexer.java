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
                message = invalidTokenError()+ c +"(ascii: "+(int) c+"). Scanning aborted";
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
                        message = classErrorName()+ c +"(ascii "+iNextChar+") Unidentified error. Scanning aborted";
                        oList.add(message, eNodeType.Error);
                        throw new Exception(message);
                    }
                }

            }


        }
        return oList;
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
                message = unexpectedTokenError()+" "+ (char) iNextChar + " (ascii "+iNextChar+") after '-' ";
                oList.add(new cNode(message, eNodeType.Error));
                throw new Exception(message);
            }
            else if(c == '0' && (isNumber((char) iNextChar) || isLetter((char) iNextChar)))
            {
                // '0' cannot be followed by a number or letter
                // Error if iNextChar is a letter of number
                message = unexpectedTokenError() + (char) iNextChar + " (ascii "+iNextChar+") after '0' ";
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
                if(isNumber(c))
                {
                    // if c== '-' (when the function is called) this should always run on the first iteration
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
                    message = unexpectedTokenError()+" "+c+" (ascii "+(int)c+").";
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
                message = classErrorName()+" Unexpected end of string after -";
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
                message = unexpectedTokenError()+" "+c+" (ascii "+(int) c +")";
                oList.add(message, eNodeType.Error);
                throw new Exception(message);
            }
        }
    }

    private void insertAssignmentSymbol(char c, BufferedReader oReader) throws Exception
    {
        int iNextChar;
        String message = "";
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
                message = unexpectedTokenError()+c+ (char)iNextChar+" (ascii "+iNextChar+"+). Expected =";
                oList.add(message, eNodeType.Error);
                throw new Exception(message);
            }
        }
        else
        {
            // Invalid character after :
            // Was supposed to be an assignment symbol
            message = classErrorName() + " Unexpected end of program after "+c;
            oList.add(message, eNodeType.Error);
            throw new Exception(message);
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
                message = invalidTokenError()+ c +"(ascii: "+(int) c+"). Scanning aborted";
                oList.add(new cNode(message, eNodeType.Error));
                throw new Exception(message);
            }
            else
            {
                message = classErrorName() + sShortString+ c +". Invalid string name. Scanning aborted.";
                oList.add(new cNode(message, eNodeType.Error));
                throw new Exception(message);
            }
        }
        message = classErrorName() + sShortString + ". string too long. scanning aborted";
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
                String message = unexpectedTokenError()+" "+c+" (ascii "+(int)c+"). Expected Grouping symbol";
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

    private boolean isLetter(char c)
    {
        return ((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'));
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
    
    private String classErrorName()
    {
        return "[Lexical Error] ";
    }
    
    private String unexpectedTokenError()
    {
        return classErrorName()+" Unexpected token ";
    }

    private String invalidTokenError()
    {
        return classErrorName() +" Invalid Character ";
    }
}
