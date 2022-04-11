package Lexer;

import java.util.LinkedList;

public class cLinkedList
{
    private cNode m_ohead;
    private cNode m_oTail;
    private int m_iNodeCount;

    public cLinkedList()
    {
        m_ohead = null;
        m_oTail = null;
        m_iNodeCount = 0;
    }

    public cNode getM_ohead()
    {
        return m_ohead;
    }

    public void setM_ohead(cNode m_ohead)
    {
        this.m_ohead = m_ohead;
    }

    public cNode getM_oTail()
    {
        return m_oTail;
    }

    public void setM_oTail(cNode m_oTail)
    {
        this.m_oTail = m_oTail;
    }

    public int getM_iNodeCount()
    {
        return m_iNodeCount;
    }

    public void add(String pNodeValue)
    {

    }

    public void add(String pNodeValue, eNodeType pNodeType)
    {

    }
}

