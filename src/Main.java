import lexer.cLexer;
import lexer.cLinkedList;
import lexer.cNode;
import parser.cTreeNode;
import parser.eSymbolType;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main
{


    private cLinkedList oList = null;
    private cTreeNode root = null;
    private cNode currentNode = null;


    public static void main(String[] args)
    {
//        cLexer oLexer = new cLexer("./src/test.txt");
//        cLinkedList oList;
//        try
//        {
//            oList = oLexer.start();
//        } catch (Exception e)
//        {
//            System.out.println(e.getMessage());
//            System.out.println("//////////////////////////////////////////////////////////////////////////////");
//            System.out.println(oLexer.getList().toString());
//            return;
//        }
//        System.out.println("////////////////////////Lexing Complete");
//        System.out.print(oList.toString());
        System.out.println(eSymbolType.ProcDefs +" "+ first(eSymbolType.ProcDefs));
        System.out.println(eSymbolType.Algorithm +" "+ first(eSymbolType.Algorithm));
        System.out.println(eSymbolType.Instr +" "+ first(eSymbolType.Instr));
        System.out.println(eSymbolType.Loop +" "+ first(eSymbolType.Loop));

        return;
    }
    private static ArrayList<String> first(eSymbolType type)
    {
        Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        final String UDN = "-UDN";
        final String SS = "-SS";
        final String NUM = "-N";

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
}
