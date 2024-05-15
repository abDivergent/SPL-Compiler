package Node;

/**
 * SELF-DEFINED
 *
 * created to group specific tokens.
 *
 *      e.g instead of having to check if the value is 0,1,2,3,... just check if it is of type Number
 *
 */
public enum eNodeType
{
    Number,
    UserDefinedName,
    ShortString,
    Grouping,
    Assignment,
    Keyword,
    Error,
    EOC        //TODO EOC, EOC, EOSC or whatever you want to call it represents the end of string character within the cNode
}
