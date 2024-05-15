package Node;

/**
 * this node holds the tokens created by the lexer
 *
 * the tokens are also given a type and subtype (eNodeType, eNodeSubType)
 *
 */
public class cNode
{
    static int m_iNumNodes =0;
    private cNode m_oNext;
    private cNode m_oPrev;
    private int m_iId;
    private eNodeType m_eType;
    private String m_sValue;
    private eNodeSubType m_eSubType;

    public cNode(String pValue, eNodeType pType)
    {
        m_iId = m_iNumNodes++;
        m_sValue = pValue;
        m_eType = pType;
        m_eSubType = null;
        m_oPrev = null;
        m_oNext = null;
    }
    public cNode(String pValue, eNodeType pType, eNodeSubType pSubType)
    {
        m_iId = m_iNumNodes++;
        m_sValue = pValue;
        m_eType = pType;
        m_eSubType = pSubType;
        m_oPrev = null;
        m_oNext = null;
    }


    //Getters
    /////////////////////////////////////////////
    public int getId()
    {
        return m_iId;
    }

    public eNodeType getType()
    {
        return m_eType;
    }

    public String getValue()
    {
        return m_sValue;
    }

    public eNodeSubType getSubType()
    {
        return m_eSubType;
    }

    //Setters
    /////////////////////////////////////////////
    public void setId(int m_iId)
    {
        this.m_iId = m_iId;
    }

    public void setType(eNodeType m_sType)
    {
        this.m_eType = m_sType;
    }

    public void setValue(String m_sValue)
    {
        this.m_sValue = m_sValue;
    }

    @Override
    public String toString()
    {
        String sNodeDetails = "----------------------" +
                "\nID   : " + m_iId +
                "\nValue: " + m_sValue +
                "\nType : " + m_eType +
                "\nSubType :" + (m_eSubType != null ? String.valueOf(m_eSubType) : "null") +
                "\nNext :" + (m_oNext != null ? String.valueOf(m_oNext.m_iId) : "null") +
                "\nPrev :" + (m_oPrev != null ? String.valueOf(m_oPrev.m_iId) : "null");
        return sNodeDetails;
    }

    public cNode Prev()
    {
        return m_oPrev;
    }

    public cNode next()
    {
        return m_oNext;
    }
     public void next(cNode pNode)
     {
         m_oNext = pNode;
     }

     public void prev(cNode pNode)
     {
         m_oPrev = pNode;
     }

    public String matchError()
    {
        return "["
                + " value=" + this.getValue()
                + " type=" + this.getType()
                +" ]";
    }
}

