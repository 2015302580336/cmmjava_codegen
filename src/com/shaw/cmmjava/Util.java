package com.shaw.cmmjava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

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
