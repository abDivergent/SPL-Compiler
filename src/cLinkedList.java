public class cLinkedList
{
    private cNode m_oHead;
    private cNode m_oTail;
    private static int m_iNodeCount = 0;

    public cLinkedList()
    {
        m_oHead = null;
        m_oTail = null;
    }

    public cNode getHead()
    {
        return m_oHead;
    }

    public void setHead(cNode m_oHead)
    {
        this.m_oHead = m_oHead;
    }

    public cNode getTail()
    {
        return m_oTail;
    }

    public void setTail(cNode m_oTail)
    {
        this.m_oTail = m_oTail;
    }

    public int getNodeCount()
    {
        return m_iNodeCount;
    }

    public void add(cNode pNode)
    {
        if(pNode != null)
        {
            if(m_oHead == null)
            {
                m_oHead = pNode;
            }
            else
            {
                m_oTail.next(pNode);
                pNode.prev(m_oTail);
            }
            m_oTail = pNode;
        }
        m_iNodeCount++;
    }

    public void add(String pNodeValue, eNodeType pNodeType)
    {
        this.add(new cNode(pNodeValue, pNodeType));
    }

    public void add(String pNodeValue, eNodeType pNodeType, eNodeSubType pNodeSubType)
    {
        this.add(new cNode(pNodeValue, pNodeType, pNodeSubType));
    }


    @Override
    public String toString()
    {
        StringBuilder sNodeList = new StringBuilder();

        cNode temp = m_oHead;
        while(temp!= null){
            sNodeList.append("[")
                    .append(temp.getId())
                    .append("-")
                    .append(temp.getSubType() == null ? temp.getType() : temp.getSubType())
                    .append("-")
                    .append(temp.getValue())
                    .append("]\n");
            temp = temp.next();
        }
        return sNodeList.toString();
    }

    public String toHTMLString()
    {
        StringBuilder sNodeList = new StringBuilder();

        cNode temp = m_oHead;
        while(temp!= null){
            if(temp.getType() == eNodeType.Error){
                sNodeList.append("<p style='color: blue;'>").append(temp.getValue()).append("</p></div>");
                return sNodeList.toString();
            }
            sNodeList.append(temp.getId()).append(" | ").append(temp.getType()).append(" | ").append(
                    temp.getValue()).append("<br>");
            temp = temp.next();
        }

        sNodeList.append("</div>");
        return sNodeList.toString();
    }
}

