package Lexer;

public class cNode
{
    static int m_iNumNodes =0;
    public cNode m_oNext;
    public cNode m_oPrev;
    private int m_iId;
    private eNodeType m_sType;
    private String m_sValue;

    public cNode(String pValue, eNodeType pType)
    {
        m_iId = m_iNumNodes++;
        m_sType = pType;
        m_sValue = pValue;
        m_oPrev = null;
        m_oNext = null;
    }

    public cNode(String pValue)
    {
        m_iId = -1;
        m_sType = eNodeType.Error;
        m_sValue = pValue;
        m_oPrev = null;
        m_oNext = null;
        m_iNumNodes++;
    }

    //Getters
    /////////////////////////////////////////////
    public int getM_iId()
    {
        return m_iId;
    }

    public eNodeType getM_sType()
    {
        return m_sType;
    }

    public String getM_sValue()
    {
        return m_sValue;
    }

    //Setters
    /////////////////////////////////////////////
    public void setM_iId(int m_iId)
    {
        this.m_iId = m_iId;
    }

    public void setM_sType(eNodeType m_sType)
    {
        this.m_sType = m_sType;
    }

    public void setM_sValue(String m_sValue)
    {
        this.m_sValue = m_sValue;
    }

    @Override
    public String toString()
    {
        String sNodeDetails = "----------------------" +
                "\nID   : " + m_iId +
                "\nType : " + m_sType +
                "\nValue: " + m_sValue +
                "\nNext :" + (m_oNext != null ? String.valueOf(m_oNext.m_iId) : "null") +
                "\nPrev :" + (m_oPrev != null ? String.valueOf(m_oPrev.m_iId) : "null");
        return sNodeDetails;
    }
}

