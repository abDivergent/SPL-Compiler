package Sementics;

import Node.Scope;
import Node.cTreeNode;
import Node.eSymbolType;

public class Scoping
{
    int sIDCount=1;
    cTreeNode treeRoot;


    public Scoping(cTreeNode treeRoot)
    {
        this.treeRoot = treeRoot;
    }

    public cTreeNode start() throws Exception
    {
        if(treeRoot != null){
            if(treeRoot.getValue().equals(eSymbolType.SPL.name()) && !treeRoot.isTerminal() )
            {
                Scope scope = new Scope( );
                treeRoot.setScope(scope);
                for (cTreeNode child: treeRoot.getChildren())
                {
                    subTreeScope(child, scope);
                }
                return treeRoot;
            }
        }
        throw new Exception("root node is not SPL");

    }

    private void subTreeScope(cTreeNode parent, Scope parentScope)
    {
        Scope scope = parentScope;
        if (parent.getValue().equals(eSymbolType.PD.name()) && !parent.isTerminal())
        {
            scope = new Scope(scope.getID()+"."+sIDCount++);
            parentScope.addChildScope(scope);
            scope.setParentScope(parentScope);
        }

        parent.setScope(scope);

        for (cTreeNode child : parent.getChildren())
        {
            subTreeScope(child, scope);
        }
    }

    public String printTree()
    {
        if(treeRoot != null)
        {
            return printSubTree(treeRoot, "", 0);
        }
        return "tree root is null";
    }

    private String printSubTree(cTreeNode treeNode, String tabs, int index)
    {
        String treeString = tabs +" - "+ treeNode.getValue() + " " +treeNode.getScopeID() +"\n";
        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        int i = 1;
        for (cTreeNode child : treeNode.getChildren())
        {
            treeString += this.printSubTree(child, tabs, i++);
        }
        return treeString;
    }



}