package parser;

import lexer.cLinkedList;
import lexer.cNode;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class cParser
{
    final String UDN = "-UDN";
    final String SS = "-SS";
    final String NUM = "-N";

    private cLinkedList oList = null;
    private cTreeNode root = null;
    private cNode currentNode = null;
    public Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public cParser(cLinkedList oList)
    {
        this.oList = oList;
        currentNode = oList.getHead();
    }

    public cTreeNode start() throws Exception
    {
        parse();
        if(currentNode != null )
        return null;

    }

    private void match(String input) throws Exception
    {
        if(input.equals(currentNode.getValue()))
        {
           //todo return tree node
        }
        else
        {
            throw new Exception("Invalid input "+currentNode.getValue()+" Expected "+input);
        }
    }

    private cTreeNode parsex() throws Exception
    {
        if(isFirst(eSymbolType.ProcDefs, currentNode) || Follow("SPL", currentNode))
        {
            parseProcDefs();
            match("main");
            match("(");
            parseAlg();
            match("halt");
            match(";");
            parseVarDecl();
            match("}");
        }
        return null;
    }

    private cTreeNode parse() throws Exception
    {
        if(currentNode != null)
        {
            cTreeNode SPL = parseSPL();
            match("$");
            return SPL;
        }
        else
        {
            throw new Exception("[Parse] Error at " + currentNode);
        }
    }

    private cTreeNode parseSPL()
    {
        if(isFirst(eSymbolType.ProcDefs, currentNode) || follow(eSymbolType.ProcDefs))
        {

        }
    }





    private boolean isFirst(eSymbolType type, cNode node)
    {
        ArrayList<String> list = first(type);
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
                LOGGER.log(Level.WARNING, "unexpect error at idFirst() : "+node);
        }
        return false;
    }

    private ArrayList<String> first(eSymbolType type)
    {
        ArrayList<String> list = new ArrayList<>();
        switch (type)
        {
            case ProcDefs:
                list.add(null);
                list.addAll(first(eSymbolType.PD));
                break;
            case PD:
                list.add("proc");
                break;
            case Algorithm:
                list.add(null);
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
                list.add(null);
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
                list.add(null);
                list.addAll(first(eSymbolType.Dec));
                break;
            case Dec:
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
