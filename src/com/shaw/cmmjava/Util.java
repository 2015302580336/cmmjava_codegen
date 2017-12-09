package com.shaw.cmmjava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Text;

import com.shaw.cmmjava.exception.ParserException;
import com.shaw.cmmjava.model.Token;
import com.shaw.cmmjava.model.TreeNode;

public class Util {
    public static void println(String arg0) {
        System.out.println(arg0);
    }
    
    public static void println(char arg0) {
        System.out.println(arg0);
    }
    
    public static void println(Object arg0) {
        System.out.println(arg0);
    }
    
    
    public static void printTreeNode(Text text, TreeNode node) {
        printNodeWithIntent(text, node, 0);
    }
    
    private static void printNodeWithIntent(Text text, TreeNode node, int indent) {
        if (node.getType() != TreeNode.NULL) {
            int t = indent;
            while (t>0) {
                text.append("    ");
                t--;
            }
            text.append(node.toString());
            text.append(System.getProperty("line.separator"));
            switch (node.getType()) {
            case TreeNode.IF_STMT:
                if (node.getMiddle() != null) {
                    t = indent;
                    while (t>0) {
                        text.append("    ");
                        t--;
                    }
                    text.append("  THEN:");
                    text.append(System.getProperty("line.separator"));
                    printNodeWithIntent(text, node.getMiddle(), indent+1);
                }
                if (node.getRight() != null) {
                    t = indent;
                    while (t>0) {
                        text.append("    ");
                        t--;
                    }
                    text.append("  ELSE:");
                    text.append(System.getProperty("line.separator"));
                    printNodeWithIntent(text, node.getRight(), indent+1);
                }
                break;
            case TreeNode.WHILE_STMT:
                if (node.getMiddle() != null) {
                    printNodeWithIntent(text, node.getMiddle(), indent+1);
                }
                break;
            default:
                break;
            }
        }
        if (node.getNext() != null) {
            printNodeWithIntent(text, node.getNext(), indent);
        }
    }
    
    
    public static LinkedList<Token> getTokenList(String filestr) throws IOException {
        FileReader fr;
        fr = new FileReader(filestr);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filestr), "UTF-8"));
        LinkedList<Token> tokenList = Lexer.lexicalAnalyse(br);
        br.close();
        fr.close();
        return tokenList;
    }
    
    public static LinkedList<TreeNode> getNodeList(LinkedList<Token> tokenList) throws ParserException {
        LinkedList<TreeNode> nodeList = Parser.syntacticAnalyse(tokenList);
        return nodeList;
    }
}
