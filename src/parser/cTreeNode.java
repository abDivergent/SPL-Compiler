package parser;

import lexer.cNode;

import java.util.ArrayList;
import java.util.List;

public class cTreeNode
{
    public final cNode node;
    private List<cTreeNode> children;
    private final boolean isTerminal;

    public cTreeNode(cNode node, boolean isTerminal)
    {
        this.node = node;
        this.isTerminal = isTerminal;
    }

    public boolean isTerminal()
    {
        return isTerminal;
    }

    public List<cTreeNode> getChildren()
    {
        return children;
    }

}
