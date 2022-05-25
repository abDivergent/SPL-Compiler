package Sementics.Naming;

import Node.Scope;
import Node.cTreeNode;
import Node.eSymbolType;

import java.util.ArrayList;

public class VariableAnalysis
{
    private cTreeNode treeRoot;
    private ArrayList<cTreeNode> fields = new ArrayList<>();
    private ArrayList<cTreeNode> vars = new ArrayList<>();
    private ArrayList<cTreeNode> PDs = new ArrayList<>();
    private ArrayList<cTreeNode> PCalls = new ArrayList<>();
    private ArrayList<cTreeNode> arrDecs = new ArrayList<>();
    private ArrayList<cTreeNode> varDecs = new ArrayList<>();
    private int vCount = 0;
    private int aCount = 0;
    private int pCount = 0;


    public VariableAnalysis(cTreeNode treeRoot)
    {
        this.treeRoot = treeRoot;

        // initialises all the lists
        initArrays(treeRoot);

    }

    public cTreeNode start() throws Exception
    {
        if(treeRoot != null)
        {
            procedureCheck(treeRoot);

            // Rule: Procedure declaration without existence of a matching procedure call
            unusedProcedureCheck();
            // Rule: Procedure call without existence of a matching procedure declaration
            unusedPCallCheck();

            variableCheck(treeRoot);

            //  Variable declaration without existence of any matching variable usage
            unusedDeclarationCheck();
            // Rule: Variable usage without existence of any matching variable declaration
            unusedVarCheck();
            unusedFieldCheck();
        }
        return treeRoot;
    }

    /**
     * initialises the following lists
     *         Fields
     *         Vars
     *         PDs
     *         PCalls
     *         arrDecs
     *         varDecs
     *  gives semantic names to all the array and Variable declarations
     * @param node where to start, typically the root of the tree
     */
    private void initArrays(cTreeNode node)
    {
        if(node != null && !node.isTerminal())
        {
            if(node.isType(eSymbolType.Field))
                fields.add(node);
            else if (node.isType(eSymbolType.Var) && !node.getParent().isType(eSymbolType.Dec))
                vars.add(node);
            else if (node.isType(eSymbolType.PD))
            {
                node.getChildren().get(1).setSemanticName("p"+pCount++);
                PDs.add(node);
            }
            else if (node.isType(eSymbolType.PCall))
                PCalls.add(node);
            else if (node.isType(eSymbolType.Dec))
            {
                if(node.firstChild().getValue().endsWith("arr"))
                {
                    node.lastChild().firstChild().setSemanticName("a"+aCount++);
                    arrDecs.add(node);
                }
                else
                {
                    node.lastChild().firstChild().setSemanticName("v"+vCount++);
                    varDecs.add(node);
                }
            }

            for (cTreeNode child : node.getChildren())
            {
                initArrays(child);
            }
        }

    }

    /////////////////////////////////////////////// VARIABLE CHECKS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void variableCheck(cTreeNode node) throws Exception
    {

        //Rule: The declaration of some variable v can never be in any “offspring”-scope of v’s scope
        if(node != null && !node.isTerminal())
        {
            if(node.isType(eSymbolType.Var))
            {
                String name = node.firstChild().getValue();
                // parent not Dec node
                if( !node.getParent().isType(eSymbolType.Dec))
                {


                    // Rule: The declaration of v can only be either in v’s own scope, or in some “ancestor”-scope
                    // This rule DOES NOT limit te declaration of a variable to one
                    if (incorrectScopeDeclaration(node))
                    {
                        // Rule: The declaration of some variable v can never be in any “offspring”-scope of v’s scope.
                        if (offspringDeclaration(node))
                            throw new Exception("[Declaration Error] variable \""+ name +"\" used before declaration");

                        throw new Exception("[Declaration Error] variable \""+name+"\" cannot be used before declaration");
                    }

                    /*
                      Rule:  If there is no declaration for v in its own scope, there must exist a matching
                             declaration in any of v’s “ancestor”

                      Rule:   If there is a declaration for v in its own scope and another declaration for v in some
                              “ancestor”-scope, then the declaration in v’s own scope is the “matching” declaration:
                              local variable

                      Rule:   If there is no declaration for v in its own scope, and there are two declarations for
                              v at two different hierarchy-levels in two different “ancestor”-scopes, then the
                              “nearer” declaration is the “matching” declaration
                     */
                    if (hasNoDeclaration(node))
                        throw new Exception("[Declaration Error] no declaration for \""+name);
                }
            }
            if(node.isType(eSymbolType.Dec))
            {
                String name = node.lastChild().firstChild().getValue();

                //  Rule:  Within any one scope, there cannot be more than one declaration for the same variable v
                if(multipleScopeDeclaration(node))
                    throw new Exception("[Sementics.Naming Error] variable \""+name+"\" has already been declared ");

            }
            if(node.isType(eSymbolType.Field))
            {
                String name = node.firstChild().getValue();

                if (incorrectScopeDeclaration(node))
                {
                    if(offspringDeclaration(node))
                        throw new Exception("[Declaration Error] array \""+name+"\" cannot be used before declaration");

                    throw new Exception("[Declaration Error] arr \""+name+"\" cannot be used before declaration");
                }

                if (hasNoDeclaration(node))
                    throw new Exception("[Declaration Error] no declaration for \""+name);


            }

            for (cTreeNode child : node.getChildren())
            {
                variableCheck(child);
            }
        }
    }

    private void unusedVarCheck() throws Exception
    {
        ArrayList<cTreeNode> newVars = new ArrayList<>();
        for (cTreeNode var: vars)
        {
            if(isDeclared(var))
                newVars.add(var);
            else
            {
                String name = var.getValue();
                throw new Exception("[APPL-DECL Error] variable \""+name+"\" is not defined");
            }
        }
        vars = newVars;
    }

    private void unusedFieldCheck() throws Exception
    {
        ArrayList<cTreeNode> newFields = new ArrayList<>();
        for (cTreeNode var: fields)
        {
            if(isDeclared(var))
                newFields.add(var);
            else
            {
                String name = var.getValue();
                throw new Exception("[APPL-DECL Error] variable \""+name+"\" is not defined");
            }
        }
        fields = newFields;
    }

    private void unusedDeclarationCheck() throws Exception
    {
        ArrayList<cTreeNode> newVarDecs = new ArrayList<>();
        for (cTreeNode varDec : varDecs)
        {
            if(isUsed(varDec))
                newVarDecs.add(varDec);
            else
            {
                String name = varDec.lastChild().firstChild().getValue();
                throw new Exception("[DECL-APPL Error] variable \""+name+"\" is never used");
            }
        }
        varDecs = newVarDecs;

        ArrayList<cTreeNode> newArrDecs = new ArrayList<>();
        for (cTreeNode arrDec : arrDecs)
        {
            if(isUsed(arrDec))
                newArrDecs.add(arrDec);
            else
            {
                String name = arrDec.lastChild().firstChild().getValue();
                throw new Exception("[DECL-APPL Error] array \""+name+"\" is never used");
            }
        }
        arrDecs = newArrDecs;
    }

    private boolean hasNoDeclaration(cTreeNode node) throws Exception
    {
        ArrayList<cTreeNode> declarations;
        if(node.isType(eSymbolType.Field))
            declarations = arrDecs;
        else
            declarations = varDecs;
        Scope currentScope = node.getScope();
        while (currentScope != null)
        {
            for (cTreeNode varDec : declarations)
            {
                if(varDec.getScopeID().equals(currentScope.getID()))
                {
                    if(sameNameVars(varDec.lastChild(), node))
                    {
                        node.firstChild().setSemanticName(getSemanticName(varDec));
                        return false;    // return false because it does have a Declaration in scope or ancestor
                    }
                }
            }
            currentScope = currentScope.getParentScope();
        }
        return true;
    }

    private String getSemanticName(cTreeNode node) throws Exception
    {
        if(node.isType(eSymbolType.PD) || node.isType(eSymbolType.PCall))
            return node.getChildren().get(1).getSemanticName();
        if(node.isType(eSymbolType.Dec))
            return node.lastChild().firstChild().getSemanticName();
        if(node.isType(eSymbolType.Field) || node.isType(eSymbolType.Var))
            return node.firstChild().getSemanticName();
        else
            throw new Exception("Node has no semantic name "+node.getValue());
    }

    private boolean multipleScopeDeclaration(cTreeNode node)
    {
        ArrayList<cTreeNode> declarations;

        if(node.firstChild().getValue().equals("arr"))
            declarations = arrDecs;
        else
            declarations = varDecs;

        for (cTreeNode dec :declarations)
        {
            if(dec.getScopeID().equals(node.getScopeID()) && node.getID() != dec.getID())
            {
                String name1 = dec.lastChild().firstChild().getValue();
                String name2 = node.lastChild().firstChild().getValue();
                if(name1.equals(name2))
                    return true;
            }
        }
        return false;

    }

    private boolean offspringDeclaration(cTreeNode varNode) throws Exception
    {
        ArrayList<cTreeNode> declarations;
        if(varNode.isType(eSymbolType.Field))
            declarations = arrDecs;
        else
            declarations = varDecs;

        for (cTreeNode varDec : declarations)
        {
            if (sameNameVars(varDec.lastChild(), varNode))
            {
                // if declaration happens in an offspring scope of variable
                // ALT: if variable is used in a scope where Declaration has not happened
                // ALT:  if varNode scope is ancestor of varDec

                if(isAncestorScope(varNode.getScope(), varDec.getScope().getParentScope()))
                {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean incorrectScopeDeclaration(cTreeNode varNode) throws Exception
    {
        ArrayList<cTreeNode> declarations;
        if(varNode.isType(eSymbolType.Field))
            declarations = arrDecs;
        else
            declarations = varDecs;

        // loop through each variable node in variables
        for (cTreeNode varDec : declarations)
        {
            // if the varDec has the same name as varNode
            if(sameNameVars(varDec.lastChild(), varNode))
            {
                // if the varDec is parent of varNode
                if(varNode.getScopeID().equals(varDec.getScopeID()) || isAncestorScope(varDec.getScope(), varNode.getScope()))
                    return false;


            }
        }
        return true;
    }

    /** Checks whether the given parentScope is a parent of the given offspringScope
     *
     * @param parentScope scope given as ancestor
     * @param offspringScope scope given as offSpring
     * @return TRUE     if the parentScope is a parent of offspringScope
     *         FALSE    if the parentScope is not a parent of offspringScope
     */
    private boolean isAncestorScope(Scope parentScope, Scope offspringScope)
    {
        // parent of root scope is null
        // loop while parent of root scope has not been reached
        while (offspringScope != null && parentScope != null)
        {
            // if the parentScope is equal to offspringScope
            // since offspringScope keeps "getting older" (going up the tree), when parentScope = offspringScope then
            // parentScope is an ancestor of offspringScope.
            if(offspringScope.getID().equals(parentScope.getID()))
                return true;

            // go up in the tree
            offspringScope = offspringScope.getParentScope();
        }
        return false;
    }

    private boolean sameNameVars(cTreeNode var1, cTreeNode var2) throws Exception
    {

        if( var1 != null && var2 != null)
        {
            String name1 = var1.firstChild().getValue();
            String name2 = var2.firstChild().getValue();
            return name1.equals(name2);
        }
        else
        {
            if (var1 == null && var2 == null)
                throw new Exception("[Null Error] cannot compare var names for null objects");

            String error = null;
            if (var1 == null)
                error = var2.firstChild().getValue();
            if (var2 == null)
                error = var1.firstChild().getValue();

            throw new Exception("[Null Error] cannot compare variable \"" + error + "\" with null");
        }
    }

    private boolean isUsed(cTreeNode dec) throws Exception
    {
        ArrayList<cTreeNode> arr1;
        if(dec.firstChild().getValue().equals("arr"))
            arr1 = fields;
        else
            arr1 = vars;

        for (cTreeNode var : arr1)
        {
            if(getSemanticName(var) == null)
                return false;
            if(getSemanticName(var).equals(getSemanticName(dec)))
                return true;
        }
        return false;
    }

    private boolean isDeclared(cTreeNode node) throws Exception
    {
        ArrayList<cTreeNode> varNodes;
        if (node.isType(eSymbolType.Field))
            varNodes = arrDecs;
        else
            varNodes = varDecs;

        for (cTreeNode dec : varNodes)
        {
            if(getSemanticName(dec).equals(getSemanticName(node)))
                return true;
        }
        return false;
    }


    /////////////////////////////////////////////// PROCEDURE CHECKS ///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void procedureCheck(cTreeNode node) throws Exception
    {
        if(node != null && !node.isTerminal())
        {
            if(node.isType(eSymbolType.PD))
            {
                String name = node.getChildren().get(1).getValue();

                // Rule: The unique “main” cannot be a UserDefinedName for any declared procedure in an SPL program
                if(name.equals("name"))
                    throw new Exception("Procedure name cannot be \"main\"");

                // Rule: Let S be a scope opened by a procedure declaration with some UserDefinedName=u. Let
                //S1, S2, ..., Sn be the Child-scopes in C of S. Then no procedure declaration in any of S1,
                //S2, ... Sn may have the same name u.
                if(hasSameNameChild(node))
                    throw new Exception("[Sementics.Naming Error] a parent procedure with the name \"" +name+"\" already exists");

                // Rule: Procedure declarations in Sibling-scopes must have different names
                if(hasSameNameSiblings(node))
                    throw new Exception("[Sementics.Naming Error] a sibling procedure with the name \""+name+"\" already exists");


                // if(!hasCorrespondingPCall(node))
                //     throw new Exception("[Sementics.Naming Error] procedure \""+name+"\" has no matching procedure call within range");


            }

            if(node.isType(eSymbolType.PCall))
            {
                String name = node.getChildren().get(1).getValue();

                // Rule: The unique “main” cannot be a UserDefinedName for any declared procedure in an SPL program
                if(name.equals("main"))
                    throw new Exception("Procedure name cannot be \"main\"");

                //Rule: Any procedure can only call itself or a sub-procedure that is declared in an immediate
                // Child-scope
                if(hasCorrespondingPD(node))
                {
                    cTreeNode pd = getCorrespondingPD(node);
                    if(pd != null)
                        node.getChildren().get(1).setSemanticName(getSemanticName(pd));
                }else
                    throw new Exception("[Sementics.Naming Error] no procedure with the name \""+name+"\" was not found within procedure calling range");
            }

            for (cTreeNode child : node.getChildren())
            {
                procedureCheck(child);
            }
        }


    }

    private void unusedProcedureCheck() throws Exception
    {
        ArrayList<cTreeNode> newPDs = new ArrayList<>();
        for (cTreeNode pd : PDs)
        {
            if(isCalled(pd))
                newPDs.add(pd);
            else
            {
                String name = pd.getChildren().get(1).getValue();
                throw new Exception("[DECL-APPL Error] procedure \""+name+"\" is never called");
            }
        }
        PDs = newPDs;
    }

    private void unusedPCallCheck() throws Exception
    {
        ArrayList<cTreeNode> newPCalls = new ArrayList<>();
        for (cTreeNode pCall : PCalls)
        {
            if (isUsedPCall(pCall))
                newPCalls.add(pCall);
            else
            {
                String name = pCall.getChildren().get(1).getValue();
                throw new Exception("[APPL-DECL Error] no matching procedure for \""+name+"\"");
            }
        }
        PCalls = newPCalls;
    }

    private boolean hasCorrespondingPD(cTreeNode node) throws Exception
    {
        return getCorrespondingPD(node) != null;
    }

    private cTreeNode getCorrespondingPD(cTreeNode node) throws Exception
    {
        if(node != null )
        {
            Scope pScope = node.getScope();
            if(pScope != null )
            {
                for (cTreeNode pd : PDs)
                {
                    // if PD node's scope = node's scope or
                    // PD node's scope = node scope's parent scope
                    if(pd.getScope().getParentScope() == null)
                        throw new Exception("[Scoping Error] PD cannot have a cope of 0");
                    if (pd.getScope().getParentScope().getID().equals(node.getScopeID()) || pd.getScopeID().equals(node.getScopeID()))
                    {
                        if (sameNameProc(pd, node))
                            return pd;
                    }
                }
            }
        }
        return null;
    }

    /**
     * checks whether any procNodes within the child scope of the currentNode  have the same name as the currentNode
     *
     * @param currentNode node whose child scopes are to be checked
     * @return True if procNode with same name exists (in child scope)
     *         False if NO procNode with same name exists (in child scope)
     */
    private boolean hasSameNameChild(cTreeNode currentNode)
    {
        // child scopes to be checked
        ArrayList<Scope> childScopes = currentNode.getScope().getChildScopes();

        //Loop through all PDs
        for (cTreeNode proc : PDs)
        {
            // If the proc's scope is within child scopes
            if(childScopes.contains(proc.getScope()))
            {
                // if the proc is has the same name as current node
                // Second Value should not be needed, theoretically since
                if (sameNameProc(proc, currentNode) /*&& proc.getID() != currentNode.getID()*/)
                    return true;
            }
        }
        // No node within child scopes was found to have the same name as currentNode
        return false;
    }

    /**
     *  check above  (hasSameNameChild() method)
     *
     */
    private boolean hasSameNameSiblings(cTreeNode node) throws Exception
    {
        if(node.getScopeID().equals("0"))
        {
            throw new Exception("Proc cannot have scope zero");
        }else
        {
            ArrayList<Scope> siblingScopes = node.getScope().getParentScope().getChildScopes();
            for (cTreeNode proc : PDs )
            {
                if(siblingScopes.contains(proc.getScope()))
                {
                    if(sameNameProc(proc, node) && !proc.getScopeID().equals(node.getScopeID()))
                        return true;
                }
            }
        }
        return false;
    }
    private boolean sameNameProc(cTreeNode node1, cTreeNode node2)
    {
        String name1 = node1.getChildren().get(1).getValue();
        String name2 = node2.getChildren().get(1).getValue();
        return name1.equals(name2);
    }


    private boolean isCalled(cTreeNode pd) throws Exception
    {
        for (cTreeNode pcall : PCalls)
        {
            if (getSemanticName(pcall).equals(getSemanticName(pd)))
                return true;
        }
        return  false;
    }

    private boolean isUsedPCall(cTreeNode node) throws Exception
    {
        if(getSemanticName(node) != null && !getSemanticName(node).equals(""))
        {
            return hasCorrespondingPD(node);
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String printTree(cTreeNode treeNode)
    {
        if(treeNode != null)
        {
            return printSubTree(treeNode, "", 0);
        }
        return "tree root is null";
    }

    private String printSubTree(cTreeNode treeNode, String tabs, int index)
    {
        String displayName = "";
        if(treeNode.getSemanticName() != null )
            displayName = ", vNum="+ treeNode.getSemanticName();
        String treeString = tabs +treeNode.getValue()+ "\n";
        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        int i = 1;
        for (cTreeNode child : treeNode.getChildren())
        {
            treeString += this.printSubTree(child, tabs, i++);
        }
        return treeString;
    }

//    private void removeGrouping(cTreeNode node)
//    {
//        if(node != null)
//        {
//            boolean changed = true;
//            ArrayList<cTreeNode> newChildren = new ArrayList<>();
//            for (int i = 0; i < node.getChildren().size(); i++)
//            {
//                if(node.getChildren().get(i) != null)
//                {
//                    if (!isRemovable(node.getChildren().get(i)))
//                    {
//                        newChildren.add(node.getChildren().get(i));
//                    }
//                }
//            }
//
//            newChildren.trimToSize();
//            node.setChildren(newChildren);
//            for (int i = 0; i < node.getChildren().size(); i++)
//            {
//                removeGrouping(node.getChildren().get(i));
//            }
//        }
//    }
//
//    private boolean isRemovable(cTreeNode node)
//    {
//        String[] removableSymbol = {"{", "}", "(", ")", ";" , ",", "[", "]", ":=", "main", "halt", "proc", "return",
//                "if","then", "else", "do", "while", "until", "output", "call","not", "and", "or","eq", "larger","add", "sub", "mult"};
//        for (String s : removableSymbol)
//        {
//            if (node.node.getValue().equals(s))
//                return true;
//        }
//        return false;
//    }


}
