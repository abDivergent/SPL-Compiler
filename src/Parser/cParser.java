package Parser;

import Lexer.cLinkedList;
import Node.cNode;
import Node.cTreeNode;
import Node.eNodeType;
import Node.eSymbolType;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class cParser
{
    final String UDN = "-UDN";
    final String SS = "-SS";
    final String NUM = "-N";

    private cNode currentNode;
    private String treeString = "";
    private cTreeNode treeRoot = null;
    public Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public cParser(cLinkedList oList)
    {
        oList.add(new cNode("$", eNodeType.EOC));
        currentNode = oList.getHead();
    }

    public cTreeNode start() throws Exception
    {
        if (currentNode != null)
        {
            treeRoot = parse();
            removeGrouping(treeRoot);
            pruneSubTree(treeRoot);
            return treeRoot;
        }
        else
            throw new Exception("Invalid input list");
    }

    private cTreeNode match(String input) throws Exception
    {
        if( (input.equals(UDN) && currentNode.getType() == eNodeType.UserDefinedName) ||
                (input.equals(SS) && currentNode.getType() == eNodeType.ShortString) ||
                (input.equals(NUM) && currentNode.getType() == eNodeType.Number) ||
                input.equals(currentNode.getValue()))
        {
            cTreeNode temp = new cTreeNode(currentNode);
            currentNode = currentNode.next();
            return temp;
        }
        else
        {
            switch (input)
            {
                case UDN:
                    input = "a userDefinedName token";
                    break;
                case SS:
                    input = "a ShortString Token";
                    break;
                case NUM:
                    input = "a Number Token";
                    break;
            }
            throw new Exception("Invalid token "+currentNode.matchError() +", Expected "+input);
        }
    }

    private cTreeNode parse() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(currentNode != null)
        {
            if(isFirst(eSymbolType.SPL, currentNode))
            {
                cTreeNode temp = parseSPL();
                match("$");
                return temp;
            }else if (currentNode.getValue().equals("$"))
            {
                match("$");
                return null;
            }
            else
                throw new Exception("[Parse Error] SPL has no action for "+currentNode);
        }
        else
            throw new Exception("[Parse] Error at first node is null");
    }

    private cTreeNode parseSPL() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.ProcDefs, currentNode))
            children = addChild(parseProcDefs(), children, eSymbolType.ProcDefs);

        if (currentNode.getValue().equals("main"))
        {
            children.add(match("main"));
            children.add(match("{"));
            if (isFirst(eSymbolType.Algorithm, currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);
            children.add(match("halt"));
            children.add(match(";"));
            if(isFirst(eSymbolType.VarDecl, currentNode))
                children = addChild(parseVarDecl(), children, eSymbolType.VarDecl);
            children.add(match("}"));
            return new cTreeNode(new cNode(eSymbolType.SPL.name(), null), children);
        }
        else
            throw new Exception("Error at SPL: no action for "+currentNode);
    }

    private cTreeNode parseVarDecl() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.Dec, currentNode))
        {
            children = addChild(parseDec(), children, eSymbolType.Dec);
            children.add(match(";"));

            if (isFirst(eSymbolType.VarDecl, currentNode))
                children = addChild(parseVarDecl(), children, eSymbolType.VarDecl);

            return new cTreeNode(new cNode(eSymbolType.VarDecl.name(), null), children);
        }
        else
            throw new Exception("Parse Error] VarDecl has no action for "+currentNode);
    }

    private cTreeNode parseDec() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.TYP, currentNode))
        {
            children = addChild(parseTYP(), children, eSymbolType.TYP);
            children = addChild(parseVar(), children, eSymbolType.Var);
        }else if(currentNode.getValue().equals("arr"))
        {
            children.add(match("arr"));
            children = addChild(parseTYP(), children, eSymbolType.TYP);
            children.add(match("["));
            children = addChild(parseConst(), children, eSymbolType.Const);
            children.add(match("]"));
            children = addChild(parseVar(), children, eSymbolType.Var);
        }
        else
            throw new Exception("[Parse Error] Dec has no action for "+currentNode);
        
        return new cTreeNode(new cNode(eSymbolType.Dec.name(), null), children);
    }

    private cTreeNode parseVar() throws Exception
    {
        if (isFirst(eSymbolType.Var, currentNode))
            return match(UDN);
        else
            throw new Exception("[Parse Error] Var has no action for "+currentNode);
    }

    private cTreeNode parseTYP() throws Exception
    {
        switch (currentNode.getValue())
        {
            case "num":
                return match("num");
            case "bool":
                return match("bool");
            case "string":
                return match("string");
            default:
                throw new Exception("[Parse Error] TYP has no action for "+currentNode);
        }
    }

    private cTreeNode parseAlg() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.Instr, currentNode))
        {
            children = addChild(parseInstr(), children, eSymbolType.Instr);
            children.add(match(";"));
            if (isFirst(eSymbolType.Algorithm, currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);
            return new cTreeNode(new cNode(eSymbolType.Algorithm.name(), null), children) ;
        }
        else
            throw new Exception("[Parse Error] Alg has no action for "+currentNode );
    }

    private cTreeNode parseInstr() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.Assign, currentNode))
            children = addChild(parseAssign(), children, eSymbolType.Assign);
        else if (isFirst(eSymbolType.Branch, currentNode))
            children = addChild(parseBranch(), children, eSymbolType.Branch);
        else if (isFirst(eSymbolType.Loop, currentNode))
            children = addChild(parseLoop(), children, eSymbolType.Loop);
        else if (isFirst(eSymbolType.PCall, currentNode))
            children = addChild(parsePCall(), children, eSymbolType.PCall);
        else
            throw new Exception("[Parse Error] Instr has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.Instr.name(), null), children);
    }

    private cTreeNode parsePCall() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if (currentNode.getValue().equals("call"))
        {
            children.add(match("call"));
            children.add(match(UDN));
            return new cTreeNode(new cNode(eSymbolType.PCall.name(), null), children);
        }
        else
            throw new Exception("[Parse Error] PCall has no action for "+currentNode);
    }

    private cTreeNode parseLoop() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(currentNode.getValue().equals("do"))
        {
            children.add(match("do"));
            children.add(match("{"));
            if(isFirst(eSymbolType.Algorithm, currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);

            children.add(match("}"));
            children.add(match("until"));
            children.add(match("("));
            children = addChild(parseExpr(), children, eSymbolType.Expr);
            children.add(match(")"));

        }else if (currentNode.getValue().equals("while"))
        {
            children.add(match("while"));
            children.add(match("("));
            children = addChild(parseExpr(), children, eSymbolType.Expr);
            children.add(match(")"));
            children.add(match("do"));
            children.add(match("{"));
            if(isFirst(eSymbolType.Algorithm ,currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);

            children.add(match("}"));
        }
        else
            throw new Exception("[Parse Error] Loop has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.Loop.name(), null), children);
    }

    private cTreeNode parseExpr() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if (isFirst(eSymbolType.Const, currentNode))
            children = addChild(parseConst(), children, eSymbolType.Const);
        else if (isFirst(eSymbolType.UnOp, currentNode))
            children = addChild(parseUnOp(), children, eSymbolType.UnOp);
        else if (isFirst(eSymbolType.BinOp, currentNode))
            children = addChild(parseBinOp(), children, eSymbolType.BinOp);
        else if (isFirst(eSymbolType.Var,currentNode))
        {
            // First(Var) = First(Field)
            // Solution, call Field if the following node is "["
            if(currentNode.next().getValue().endsWith("["))
                children = addChild(parseField(), children, eSymbolType.Field);
            else
                children = addChild(parseVar(), children, eSymbolType.Var);
        }
        else
            throw new Exception("[Parse Error] Expr has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.Expr.name(), null), children);
    }

    private cTreeNode parseField() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.Field, currentNode))
        {
            children.add(match(UDN));
            children.add(match("["));
            if (isFirst(eSymbolType.Var, currentNode))
                children = addChild(parseVar(), children, eSymbolType.Var);
            else if (isFirst(eSymbolType.Const, currentNode))
                children = addChild(parseConst(), children, eSymbolType.Const);
            else
                throw new Exception("[Parse Error] expected Var or Const but received "+currentNode);
            children.add(match("]"));
        }
        else
            throw new Exception("[Parse Error] Field has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.Field.name(), null), children);
    }

    private cTreeNode parseBinOp() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        switch (currentNode.getValue())
        {
            case "and":
                children.add(match("and"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            case "or":
                children.add(match("or"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            case "eq":
                children.add(match("eq"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            case "larger":
                children.add(match("larger"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            case "add":
                children.add(match("add"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            case "sub":
                children.add(match("sub"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            case "mult":
                children.add(match("mult"));
                children.add(match("("));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(","));
                children = addChild(parseExpr(), children, eSymbolType.Expr);
                children.add(match(")"));
                break;
            default:
                throw new Exception("[Parse Error]  BinOp has no action for " + currentNode);
        }

        return new cTreeNode(new cNode(eSymbolType.BinOp.name(), null), children);
    }

    private cTreeNode parseUnOp() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(currentNode.getValue().equals("input"))
        {
            children.add(match("input"));
            children.add(match("("));
            children = addChild(parseVar(), children, eSymbolType.Var);
            children.add(match(")"));
        }
        else if (currentNode.getValue().equals("not"))
        {
            children.add(match("not"));
            children.add(match("("));
            children = addChild(parseExpr(), children, eSymbolType.Expr);
            children.add(match(")"));
        }
        else
            throw new Exception("[Parse Error] UnOp has no action for "+currentNode );

        return new cTreeNode(new cNode(eSymbolType.UnOp.name(), null), children);
    }

    private cTreeNode parseConst() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(currentNode.getType() == eNodeType.ShortString)
            children.add(match(SS));
        else if (currentNode.getType() == eNodeType.Number)
            children.add(match(NUM));
        else if (currentNode.getType() == eNodeType.Keyword && currentNode.getValue().equals("true"))
            children.add(match("true"));
        else if (currentNode.getType() == eNodeType.Keyword && currentNode.getValue().equals("false"))
            children.add(match("false"));
        else
            throw new Exception("[Parse Error] Const has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.Const.name(), null), children);
    }

    private cTreeNode parseBranch() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.Branch, currentNode))
        {
            children.add(match("if"));
            children.add(match("("));
            children = addChild(parseExpr(), children, eSymbolType.Expr);
            children.add(match(")"));
            children.add(match("then"));
            children.add(match("{"));
            if(isFirst(eSymbolType.Algorithm, currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);

            children.add(match("}"));
            if (isFirst(eSymbolType.Alt, currentNode))
                children = addChild(parseAlt(), children, eSymbolType.Alt);

            return new cTreeNode(new cNode(eSymbolType.Branch.name(), null), children);
        }
        else
            throw new Exception("[Parse Error] Branch has no action for "+currentNode);
    }

    private cTreeNode parseAlt() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.Alt, currentNode))
        {
            children.add(match("else"));
            children.add(match("{"));
            if(isFirst(eSymbolType.Algorithm, currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);

            children.add(match("}"));
        }
        else
            throw new Exception("[Parse Error] Alternat has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.Alt.name(), null), children);
    }

    private cTreeNode parseAssign() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if (isFirst(eSymbolType.LHS, currentNode))
        {
            children = addChild(parseLHS(), children, eSymbolType.LHS);
            children.add(match(":="));
            children = addChild(parseExpr(), children, eSymbolType.Expr);

            return new cTreeNode(new cNode(eSymbolType.Assign.name(), null), children);
        }
        else
            throw new Exception("Parse Error] Assign has no action for "+currentNode);
    }

    private cTreeNode parseLHS() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if (currentNode.getValue().equals("output"))
            children.add(match("output"));
        else if (isFirst(eSymbolType.Var, currentNode))
        {
            if (currentNode.next().getValue().equals("["))
                children = addChild(parseField(), children, eSymbolType.Field);
            else
                children = addChild(parseVar(), children, eSymbolType.Var);
        }
        else
            throw new Exception("[Parse Error] LHS has no action for "+currentNode);

        return new cTreeNode(new cNode(eSymbolType.LHS.name(), null), children);
    }

    private cTreeNode parseProcDefs() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.PD,currentNode))
        {
            children = addChild(parsePD(), children, eSymbolType.PD);
            children.add(match(","));
            if(isFirst(eSymbolType.ProcDefs, currentNode))
                children = addChild(parseProcDefs(), children, eSymbolType.ProcDefs);

            return new cTreeNode(new cNode(eSymbolType.ProcDefs.name(), null), children);
        }
        else
            throw new Exception("[Parser Error] ProcDefs has no action for "+currentNode);
    }

    private cTreeNode parsePD() throws Exception
    {
        ArrayList<cTreeNode> children = new ArrayList<>();
        if(isFirst(eSymbolType.PD, currentNode))
        {
            children.add(match("proc"));
            children.add(match(UDN));
            children.add(match("{"));

            if(isFirst(eSymbolType.ProcDefs, currentNode))
                children = addChild(parseProcDefs(), children, eSymbolType.ProcDefs);

            if(isFirst(eSymbolType.Algorithm, currentNode))
                children = addChild(parseAlg(), children, eSymbolType.Algorithm);

            children.add(match("return"));
            children.add(match(";"));

            if(isFirst(eSymbolType.VarDecl, currentNode))
                children = addChild(parseVarDecl(), children, eSymbolType.VarDecl);

            children.add(match("}"));

            return new cTreeNode(new cNode(eSymbolType.PD.name(), null), children);
        }
        else
            throw new Exception("[Parse Error] PD has no action for "+currentNode);
    }

    private ArrayList<cTreeNode> addChild(cTreeNode child, ArrayList<cTreeNode> children, eSymbolType type) throws Exception
    {
        if(child == null)
            throw new Exception(type+" cannot be null at "+currentNode);
        children.add(child);
        return children;
    }


    private boolean isFirst(eSymbolType type, cNode node)
    {
        ArrayList<String> list = first(type);
        switch (node.getType())
        {
            case EOC:
                return false;
            case Number:
                return list.contains(NUM);
            case UserDefinedName:
                return  list.contains(UDN);
            case ShortString:
                return list.contains(SS);
            case Keyword:
            case Grouping:
                return list.contains(node.getValue());
            default:
                LOGGER.log(Level.WARNING, "unexpect error at isFirst() : "+node);
        }
        return false;
    }

    private ArrayList<String> first(eSymbolType type)
    {
        ArrayList<String> list = new ArrayList<>();
        switch (type)
        {
            case SPL:
                list.add("main");
                list.addAll(first(eSymbolType.ProcDefs));
            case ProcDefs:
                list.addAll(first(eSymbolType.PD));
                break;
            case PD:
                list.add("proc");
                break;
            case Algorithm:
                list.addAll(first(eSymbolType.Instr));
                break;
            case Instr:
                list.addAll(first(eSymbolType.Assign));
                list.addAll(first(eSymbolType.Branch));
                list.addAll(first(eSymbolType.Loop));
                list.addAll(first(eSymbolType.PCall));
                break;
            case Assign:
                list.addAll(first(eSymbolType.LHS));
                break;
            case LHS:
                list.add("output");
                list.addAll(first(eSymbolType.Var));
                list.addAll(first(eSymbolType.Field));
                break;
            case Var:
            case Field:
                list.add(UDN);
                break;
            case Branch:
                list.add("if");
                break;
            case Loop:
                list.add("do");
                list.add("while");
                break;
            case PCall:
                list.add("call");
                break;
            case Alt:
                list.add("else");
                break;
            case Expr:
                list.addAll(first(eSymbolType.Const));
                list.addAll(first(eSymbolType.Var));
                list.addAll(first(eSymbolType.Field));
                list.addAll(first(eSymbolType.UnOp));
                list.addAll(first(eSymbolType.BinOp));
                break;
            case Const:
                list.add(SS);
                list.add(NUM);
                list.add("true");
                list.add("false");
                break;
            case UnOp:
                list.add("input");
                list.add("not");
                break;
            case BinOp:
                list.add("and");
                list.add("or");
                list.add("eq");
                list.add("larger");
                list.add("add");
                list.add("sub");
                list.add("mult");
                break;
            case VarDecl:
                list.addAll(first(eSymbolType.Dec));
                break;
            case Dec:
                list.add("arr");
                list.addAll(first(eSymbolType.TYP));
                break;
            case TYP:
                list.add("num");
                list.add("bool");
                list.add("string");
                break;
            default:
                LOGGER.log(Level.WARNING, "unexpect token in first(): "+type);
                return list;
        }
        return list;
    }

    public String printTree()
    {
        if(treeRoot != null)
        {
            printSubTree(treeRoot, "", 0);
            return treeString;
        }
        return "tree root is null";
    }

    private void printSubTree(cTreeNode treeNode, String tabs, int index)
    {
        treeString += tabs + index +" - "+ treeNode.node.getValue() + "\n";
        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        int i = 1;
        for (cTreeNode child : treeNode.getChildren())
        {
            printSubTree(child, tabs, i++);
        }
    }

    public String getTreeString()
    {
        if(treeRoot != null)
        {
            return subTreeString(treeRoot, "",0);
        }
        return "No tree generated [tree root is null]";
    }


    private String subTreeString(cTreeNode treeNode, String padding, int index)
    {
        String str = padding + index +" - "+ treeNode.node.getValue() + "\n";
        padding += treeNode.getChildren().size() > 1 ? "   |" : "    ";

        int i = 1;
        for (cTreeNode child : treeNode.getChildren())
        {
            str += subTreeString(child, padding, i++);
        }
        return str;
    }

    private void removeGrouping(cTreeNode node)
    {
        if(node != null)
        {
            boolean changed = true;
            ArrayList<cTreeNode> newChildren = new ArrayList<>();
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                if(node.getChildren().get(i) != null)
                {
                    if (!isRemovable(node.getChildren().get(i)))
                    {
                        newChildren.add(node.getChildren().get(i));
                    }
                }
            }

            newChildren.trimToSize();
            node.setChildren(newChildren);
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                removeGrouping(node.getChildren().get(i));
            }
        }
    }

    private boolean isRemovable(cTreeNode node)
    {
        String[] removableSymbol = {"{", "}", "(", ")", ";" , ",", "[", "]", ":=" };
        for (String s : removableSymbol)
        {
            if (node.node.getValue().equals(s))
                return true;
        }
        return false;
    }

    public void pruneSubTree(cTreeNode parent)
    {
        boolean changed = false;
        if(parent != null )
        {
            if (parent.getChildren().size() > 0)
            {
                for (int i = 0; i < parent.getChildren().size(); i++)
                {
                    cTreeNode node = parent.getChildren().get(i);
                    if (node.getChildren().size() == 1 && node.getChildren().get(0).getChildren().size()  <= 1)
                    {
                        parent.getChildren().add(i, node.getChildren().get(0));
                        parent.getChildren().remove(node);
                        parent.getChildren().trimToSize();
                        changed = true;
                    }
                }
            }
            if(changed)
                pruneSubTree(parent);
            else
            {
                for (int i = 0; i < parent.getChildren().size() ; i++)
                {
                    pruneSubTree(parent.getChildren().get(i));
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //................................................. DEPRECATED
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private ArrayList<String> follow(eSymbolType type)
    {
        ArrayList<String> list = new ArrayList<>();
        switch (type)
        {
            case SPL:
                list.add("$");
                break;

            case ProcDefs:
                list.add("main");
                list.add(UDN);
                list.add("return");
                list.add("if");
                list.add("do");
                list.add("while");
                list.add("output");
                list.add("call");
                break;

            case PD:
                list.add(",");
                break;

            case Algorithm:
                list.add("halt");
                list.add("}");
                list.add("return");

        }

        return list;
    }

    private boolean isFollow(eSymbolType type, cNode node)
    {
        ArrayList<String> list = follow(type);
        switch (node.getType())
        {
            case Number:
                return list.contains(NUM);
            case UserDefinedName:
                return  list.contains(UDN);
            case ShortString:
                return list.contains(SS);
            case Keyword:
                return list.contains(node.getValue());
            default:
                LOGGER.log(Level.WARNING, "unexpect error at isFollow() : "+node);
        }
        return false;
    }

}
