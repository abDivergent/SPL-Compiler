package Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The cTreeNode holds a cNode. I think i made it like this because i did not want to break the linked list made by the
 *  lexer.
 *
 *  This node is created for the parse tree by the parser
 */
public class cTreeNode
{
    public final cNode node;
    private ArrayList<cTreeNode> children;
    private final boolean isTerminal;
    private cTreeNode parent;
    private Scope scope;
    private String semanticName;

    public cTreeNode(cNode node)
    {
        this.node = node;
        this.isTerminal = true;
        this.children = new ArrayList<>();
        this.scope = null;
        this.parent = null;
        this.semanticName = null;
    }

    public cTreeNode(cNode node, ArrayList<cTreeNode> children)
    {
        this.node = node;
        this.isTerminal = false;
        this.setChildren(children);
        this.scope = null;
        this.semanticName = null;
    }

    public boolean isTerminal()
    {
        return isTerminal;
    }

    public ArrayList<cTreeNode> getChildren()
    {
        return this.children;
    }
    public void setChildren(ArrayList<cTreeNode> childNodes)
    {
        this.children = new ArrayList<>();
        for (cTreeNode child : childNodes)
        {
            this.children.add(child);
            child.setParent(this);
        }
    }

    public String getValue()
    {
        return node.getValue();
    }

    public eNodeType getType()
    {
        return node.getType();
    }

    public int getID()
    {
        return node.getId();
    }

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    public String getScopeID()
    {
        return scope.getID();
    }

    public void setParent(cTreeNode parent)
    {
        this.parent = parent;
    }

    public cTreeNode getParent()
    {
        return parent;
    }

    public boolean isType(eSymbolType type)
    {
        return getValue().equals(type.name()) && getType()==null;
    }


    public cTreeNode lastChild()
    {
        int i = children.size();
        if(i>0)
        {
            return children.get(i-1);
        }
        else
            return null;
    }

    public cTreeNode firstChild()
    {
        if(children.size() > 0)
            return children.get(0);
        else
            return null;
    }

    public String getSemanticName()
    {
        return semanticName;
    }

    public void setSemanticName(String semanticName)
    {
        this.semanticName = semanticName;
    }
}
