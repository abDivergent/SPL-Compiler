package Node;

/**
 * SELF-DEFINED
 *
 * this subtype was created to make sure there is no misspelling error that break the compiler as the enum value would
 * always be verified to exist by java
 */
public enum eNodeSubType
{
    LeftSquareBracket,
    RightSquareBracket,
    LeftBracket,
    RightBracket,
    LeftCurlyBrace,
    RightCurlyBrace,
    Comma,
    SemiColon,
    SpecialCommand,
    PDKeyword,
    Comparison,
    BooleanOp,
    NumberOp,
    IOCommand,
    VarType
}
