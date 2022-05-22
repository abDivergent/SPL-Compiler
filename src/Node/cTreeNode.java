package Node;

import java.util.ArrayList;
import java.util.List;

public class cTreeNode
{
    public final cNode node;
    private ArrayList<cTreeNode> children;
    private final boolean isTerminal;

    private Scope scope;

    public cTreeNode(cNode node)
    {
        this.node = node;
        this.isTerminal = true;
        this.children = new ArrayList<>();
        this.scope = null;
    }

    public cTreeNode(cNode node, ArrayList<cTreeNode> children)
    {
        this.node = node;
        this.isTerminal = false;
        this.setChildren(children);
        this.scope = null;
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
        this.children.addAll(childNodes);
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


}
