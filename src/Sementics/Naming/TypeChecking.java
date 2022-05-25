package Sementics.Naming;

import Node.cTreeNode;
import Node.eNodeType;
import Node.eSymbolType;

import java.util.ArrayList;

public class TypeChecking
{
    private cTreeNode treeRoot;
    private ArrayList<cTreeNode> vars = new ArrayList<>();

    public TypeChecking(cTreeNode treeRoot)
    {
        this.treeRoot = treeRoot;
    }

    public cTreeNode start() throws Exception
    {
        if(treeRoot != null)
        {
            initVarsArray(treeRoot);
            initTypes(treeRoot);
            initDecl(treeRoot);
            typeCheck(treeRoot);
        }
        return treeRoot;
    }

    private void initVarsArray(cTreeNode node)
    {
        if(node.getSemanticName() != null && !node.getSemanticName().isEmpty())
        {
            vars.add(node);
        }else
        {
            for (cTreeNode child : node.getChildren())
            {
                initVarsArray(child);
            }
        }
    }

    private void initDecl(cTreeNode node)
    {
        if(node.isType(eSymbolType.Dec))
        {
            String type;
            if(node.firstChild().getValue().equals("arr"))
            {
                type = node.getChildren().get(1).getValue();
                setvType(node.getChildren().get(1), type);
            }
            else
            {
                type = node.firstChild().getValue();
                setvType(node.firstChild(), type);
            }
            setVarType(type, node.lastChild().getSemanticName(), node.lastChild().getID());
        }
        else
        {
            for (cTreeNode child : node.getChildren())
            {
                initDecl(child);
            }
        }
    }

    private void setVarType(String type, String semanticName, int id)
    {
        for (cTreeNode var :  vars)
        {
            if(var.getSemanticName().equals(semanticName))
            {
                setvType(var, type);
            }
        }
    }

    private void setvType(cTreeNode var, String type)
    {
        switch (type)
        {
            case "num":
                type = "N";
                break;
            case "bool":
                type = "B";
                break;
            case "string":
                type = "S";
                break;
            default:
                return;
        }
        if(var.vType().equals("U"))
            var.vType(type);
    }

    private void initTypes(cTreeNode node)
    {
        if(node.isTerminal())
        {
            if(node.getType() == eNodeType.Number)
            {
                node.vType("N");
                if(!node.getValue().contains("-"))
                    node.vSubType("NN");
            }
            else if (node.getType() == eNodeType.ShortString)
            {
                node.vType("S");
            }
            else if (node.getType() == eNodeType.Keyword)
            {
                if (node.getValue().equals("true"))
                {
                    node.vType("B");
                    node.vSubType("T");
                }
                else if (node.getValue().equals("false"))
                {
                    node.vSubType("B");
                    node.vSubType("F");
                }
                else
                {
                    node.vType("C");
                }
            }
            else if( node.getType() == eNodeType.UserDefinedName)
            {
                if(node.getSemanticName().contains("p"))
                    node.vType("C");
            }
        }
        else
        {
            for (cTreeNode child: node.getChildren())
            {
                initTypes(child);
            }
        }
    }

    private void typeCheck(cTreeNode node) throws Exception
    {
        if(!node.isTerminal())
        {
            if (node.isType(eSymbolType.Var))
            {
                node.vType(node.firstChild().vType());
//                node.vSubType(node.firstChild().vSubType());
            }
            else if(node.isType(eSymbolType.Const))
            {
                node.vType(node.firstChild().vType());
                node.vSubType(node.firstChild().vSubType());
            }
            else if (node.isType(eSymbolType.Field))
            {
                if(node.lastChild().node.getType() == eNodeType.UserDefinedName)
                {
                    if (!node.lastChild().vType().equals("N"))
                        throw new Exception("[Type Error] field variables cannot be " + node.lastChild().vType());
                    else
                        node.vType("N");
                }
                else
                    if (!node.lastChild().vSubType().equals("NN"))
                        throw new Exception("[Type Error ] filed cannot have a constant with subType "+node.lastChild().vSubType());
                    else
                        node.vType("N");
                        node.vSubType("NN");
            }
            else if (node.isType(eSymbolType.BinOp))
            {
                cTreeNode node1 = node.getChildren().get(1);
                cTreeNode node2 = node.getChildren().get(2);
                typeCheck(node1);
                typeCheck(node2);
                if(node.firstChild().getValue().equals("eq"))
                {
                    if(node1.vType().equals(node2.vType()))
                        node.vType("B");
                    else
                        node.vType("F");
                }
                else if ( node.firstChild().getValue().equals("and") ||
                        node.firstChild().getValue().equals("or"))
                {
                    if(node1.vType().equals("B") && node2.vType().equals("B"))
                        node.vType("B");
                    else
                    {
                        throw new Exception("[Type Error] and()/or() parameters cannot be of type "+node1.vType()+" " +
                                "and "+node2.vType());
                    }
                }
                else if (node.firstChild().getValue().equals("add") ||
                        node.firstChild().getValue().equals("sub")||
                        node.firstChild().getValue().equals("mult"))
                {
                    if(node1.vType().equals("N") && node2.vType().equals("N"))
                        node.vType("N");
                    else
                        throw new Exception("[Type Error] add()/sub()/mult() parameters cannot be of type "+node1.vType()+" " +
                                "and "+node2.vType());
                }
                else if (node.firstChild().getValue().equals("larger"))
                {
                    if(node1.vType().equals("N") && node2.vType().equals("N"))
                        node.vType("B");
                    else
                        throw new Exception("[Type Error] larger() parameters cannot be of type "+node1.vType()+" " +
                                "and "+node2.vType());
                }
            }
            else if(node.isType(eSymbolType.Assign))
            {
                if(node.firstChild().getValue().equals("output"))
                {
                    typeCheck(node.lastChild());
                    if(node.lastChild().vType().equals("S") || node.lastChild().vType().equals("N"))
                        node.vType("M");
                    else
                        throw new Exception("[Type Error] output cannot be called with "+node.lastChild().vType());
                }
                else
                {
                    cTreeNode node1 = node.firstChild();
                    cTreeNode node2 = node.lastChild();
                    typeCheck(node1);
                    typeCheck(node2);
                    if(!node1.vType().equals(node2.vType()))
                        throw new Exception("[Type Error] cannot assign "+node2+" to "+node1);
                    if(node1.getType() == eNodeType.UserDefinedName && node2.getType() == eNodeType.UserDefinedName)
                    {
                        if(node1.getSemanticName().contains("a") && !node2.getSemanticName().contains("a"))
                            throw new Exception("[Type Error] cannot assign variable to array");
                        else if(!node1.getSemanticName().contains("a") && node2.getSemanticName().contains("a"))
                            throw new Exception("[Type Error] cannot assign array to variable");
                        else
                            node.vType(node1.vType());
                    }
                    else
                        node.vType(node1.vType());
                }
            }
            else if(node.isType(eSymbolType.UnOp))
            {
                if(node.firstChild().getValue().equals("input"))
                {
                    if(node.lastChild().vType().equals("N"))
                        node.vType("N");
                    else
                        throw new Exception("[Type Error] input cannot be of type "+node.lastChild().vType());
                }
                else
                {
                    if(node.lastChild().vType().equals("B"))
                        node.vType("B");
                    else
                        throw new Exception("[Type Error] not() parameter cannot be of type "+node.lastChild().vType());
                }
            }
            else
            {
                for (cTreeNode child: node.getChildren())
                {
                    typeCheck(child);
                }

                node.vType("C");
                for (cTreeNode child: node.getChildren())
                {
                    if(child.vType().equals("U"))
                        node.vType("U");
                        break;
                }
            }

        }
    }

    public String printTree(cTreeNode treeNode)
    {
        if(treeNode != null)
        {
            return printSubTree(treeNode, "");
        }
        return "tree root is null";
    }

    private String printSubTree(cTreeNode treeNode, String tabs)
    {
        String displayName = "";
        if(treeNode.vType() != null )
            displayName = ", vType="+ treeNode.vType();
        String treeString = tabs +treeNode.getValue()+ displayName + "\n";

        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        for (cTreeNode child : treeNode.getChildren())
        {
            treeString += this.printSubTree(child, tabs);
        }
        return treeString;
    }
}
