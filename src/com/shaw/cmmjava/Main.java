package com.shaw.cmmjava;

import java.util.LinkedList;

import com.shaw.cmmjava.exception.InterpretException;
import com.shaw.cmmjava.exception.ParserException;
import com.shaw.cmmjava.model.FourCode;
import com.shaw.cmmjava.model.TreeNode;

public class Main {

    private static SymbolTable symbolTable;
    
    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.out.println("用法:java -jar codegener.jar cmm文件名");
//        } else {
    	String file ="D:/JAVA_WORK/cmm_testCase/test9_数组排序.cmm";
            LinkedList<FourCode> codes;
            symbolTable = SymbolTable.getSymbolTable();
            try {
                symbolTable.newTable();
                codes = CodeGenerater.generateCode(file);
                symbolTable.deleteTable();
                for (FourCode code : codes) {
                    System.out.println(code.toString());
                }
            } catch (ParserException e) {
                System.out.println(e.toString());
                //e.printStackTrace();
            } catch (InterpretException e) {
                System.out.println(e.toString());
                //e.printStackTrace();
            }
        //}
    }

}
