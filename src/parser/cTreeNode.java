package parser;

import lexer.cNode;

import java.util.ArrayList;
import java.util.List;

public class cTreeNode
{
    public final cNode node;
    private List<cTreeNode> children;
    private final boolean isTerminal;

    public cTreeNode(cNode node)
    {
        this.node = node;
        this.isTerminal = true;
        children = null;
    }

    public cTreeNode(cNode node, List<cTreeNode> children)
    {
        this.node = node;
        this.isTerminal = false;
        this.setChildren(children);
    }

    public boolean isTerminal()
    {
        return isTerminal;
    }

    public List<cTreeNode> getChildren()
    {
        return children;
    }
    public void setChildren(List<cTreeNode> children)
    {
        children = new ArrayList<>();
        for (cTreeNode node : children)
        {
            this.children.add(node);
        }
    }

}
