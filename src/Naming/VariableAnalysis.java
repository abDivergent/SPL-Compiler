package Naming;

import Node.Scope;
import Node.cTreeNode;
import Node.eSymbolType;

import java.util.ArrayList;

public class VariableAnalysis
{
    private cTreeNode treeRoot;
    private ArrayList<cTreeNode> variables;
    private ArrayList<cTreeNode> procedures;

    public VariableAnalysis(cTreeNode treeRoot)
    {
        this.treeRoot = treeRoot;

        variables = new ArrayList<>();
        procedures = new ArrayList<>();
        getVarsAndProcs(treeRoot);
    }

    public cTreeNode start() throws Exception
    {
        if(treeRoot != null)
        {
            procedureCheck(treeRoot);
            //Todo implement variable checking
//            variableCheck(treeRoot);
        }
        return treeRoot;
    }

    private void getVarsAndProcs(cTreeNode current)
    {
        if(current != null && !current.isTerminal())
        {
            if(current.isType(eSymbolType.Var))
                variables.add(current);
            else if (current.isType(eSymbolType.Field))
                variables.add(current);
            else if (current.isType(eSymbolType.PCall))
                procedures.add(current);
            else if (current.isType(eSymbolType.PD))
                procedures.add(current);

            for (cTreeNode child : current.getChildren())
            {
                getVarsAndProcs(child);
            }
        }
    }
    
    private void procedureCheck(cTreeNode parent) throws Exception
    {
        if(parent.isType(eSymbolType.PD))
        {
            String name = parent.getChildren().get(1).getValue();
            if(name.equals("main"))
                throw new Exception("Procedure name cannot be \"main\"");

            if(!correctChildProcs(parent))
                throw new Exception("[Naming Error] a parent procedure with the name \"" +name+"\" alredy exists");
            if(!correctSiblingProcs(parent))
                throw new Exception("[Naming Error] a sibling procedure with the name \""+name+"\" already exists");
            if(!hasPCall(parent))
                throw new Exception("[Naming Error] procedure \""+name+"\" has no matching procedure call within range");
        }
        else if (parent.isType(eSymbolType.PCall))
        {
            String name = parent.getChildren().get(1).getValue();
            if(name.equals("main"))
                throw new Exception("Procedure name cannot be \"main\"");
            if(!hasProc(parent))
                throw new Exception("[Naming Error] procedure call \""+name+"\" has no matching procedure within range");

        }

    }

    private boolean hasProc(cTreeNode node)
    {
        Scope pScope = node.getScope().getParentScope();
        for (cTreeNode proc : procedures )
        {
            // If node is PD
            if(proc.isType(eSymbolType.PD))
            {
                // if PD node's scope = node's scope or
                // PD node's scope = node scope's parent scope
                if(proc.getScopeID().equals(pScope.getID()) || proc.getScopeID().equals(node.getScopeID()))
                    if (sameNameProc(proc, node))
                        return true;
            }
        }
        // no PD with the same name exists in current or parent scope of node
        return false;
    }

    private boolean hasPCall(cTreeNode node)
    {
        ArrayList<Scope> childScopes = node.getScope().getChildScopes();

        for (cTreeNode proc : procedures)
        {
            // If node is a PCall
            if(proc.isType(eSymbolType.PCall))
            {
                // If the PCall has the same scope as the node(PD node) or PCall is in child scope
                if(childScopes.contains(node.getScope()) || proc.getScopeID().equals(node.getScopeID()))
                {
                    // if the PCall and PD node have the same name
                    if(sameNameProc(proc, node))
                        return true;
                }
            }
        }
        // no PCall with the same name as PD node in same scope or child scopes
        return false;
    }


    private boolean correctSiblingProcs(cTreeNode node)
    {
        ArrayList<Scope> siblingScopes = node.getScope().getParentScope().getChildScopes();
        for (cTreeNode proc : procedures )
        {
            if(siblingScopes.contains(proc.getScope()) && proc.isType(eSymbolType.PD))
            {
                // if two procs are semantic siblings (check above)
                // if two different procs have the same name
                // different IDs but same value
                if(proc.getID() != node.getID() && sameNameProc(proc, node))
                    return true;
            }
        }
        return true;
    }

    private boolean correctChildProcs(cTreeNode node)
    {
        ArrayList<Scope> childScopes = node.getScope().getChildScopes();

        for (cTreeNode proc : procedures)
        {
            if(childScopes.contains(proc.getScope()) && proc.isType(eSymbolType.PD))
            {
                // if two proc is node's semantic child
                // if two different procs have the same name
                // different IDs but same value
                if (proc.getID() != node.getID() && sameNameProc(proc, node))
                    return false;
            }
        }
        return true;
    }

    private boolean sameNameProc(cTreeNode node1, cTreeNode node2)
    {
        String name1 = node1.getChildren().get(1).getValue();
        String name2 = node2.getChildren().get(1).getValue();
        return name1.equals(name2);
    }


}
