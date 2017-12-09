package com.shaw.cmmjava;

import java.util.LinkedList;
import java.util.Scanner;

import com.shaw.cmmjava.exception.InterpretException;
import com.shaw.cmmjava.exception.ParserException;
import com.shaw.cmmjava.model.FourCode;
import com.shaw.cmmjava.model.Symbol;
import com.shaw.cmmjava.model.Token;
import com.shaw.cmmjava.model.TreeNode;
import com.shaw.cmmjava.model.Value;

public class Interpreter {
    	
    private static int mLevel;
    private static int pc;
    private static SymbolTable symbolTable;

    public static void main(String[] args) {
        LinkedList<FourCode> codes;
        symbolTable = SymbolTable.getSymbolTable();
        Interpreter interpreter = new Interpreter();
        //导入cmm文件
        String file ="F:/JAVA_WORK/cmm_testCase/test3_.cmm";

//        symbolTable = SymbolTable.getSymbolTable();
//        try {
//            symbolTable.newTable();
//            codes = CodeGenerater.generateCode(file);
//            symbolTable.deleteTable();
//            for (FourCode code : codes) {
//                System.out.println(code.toString());
//            }
//        } catch (ParserException e) {
//            System.out.println(e.toString());
//            //e.printStackTrace();
//        } catch (InterpretException e) {
//            System.out.println(e.toString());
//            //e.printStackTrace();
//        }
        try {
            symbolTable.newTable();
            codes = CodeGenerater.generateCode(file);
            symbolTable.deleteTable();
            int length = codes.size();
            pc = 0;
            mLevel = 0;
            symbolTable.newTable();
            while (pc < length) {//not end
                interpreter.interpretFourCode(codes.get(pc));
            }
        } catch (ParserException e) {
            System.out.println(e.toString());
            //e.printStackTrace();
        } catch (InterpretException e) {
            System.out.println(e.toString());
            //e.printStackTrace();
        }
        symbolTable.deleteTable();
    }
    
    //===================================================================
    //
    //  FourCode解释器核心,需要生成四元式中间代码,目前调用这个
    //
    //===================================================================
    private void interpretFourCode(FourCode code) throws InterpretException {
        String instype = code.getFirst();
        if (instype.equals(FourCode.JMP)) {//跳转指令
            if (code.getSecond() == null || symbolTable.getSymbolValue(code.getSecond()).getType() == Symbol.FALSE) {//需要跳转
                pc = getValue(code.getForth()).getInt();
                return;//如果继续执行就会+1,别
            }
        }
        if (instype.equals(FourCode.READ)) {//输入指令
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            int type = symbolTable.getSymbolType(getId(code.getForth()));
            switch (type) {
            case Symbol.SINGLE_INT:
            case Symbol.ARRAY_INT:
            {
                Value value = parseValue(input);
                if (value.getType() == Symbol.SINGLE_INT) {
                    setValue(code.getForth(), value);
                } else {
                    throw new InterpretException("类型不匹配");
                }
                break;
            }
            case Symbol.SINGLE_REAL:
            case Symbol.ARRAY_REAL:
            {
                Value value = parseValue(input);
                setValue(code.getForth(), value);
                break;
            }
            case Symbol.TEMP://impossible
            default:
                break;
            }
        }
        if (instype.equals(FourCode.WRITE)) {
            int index = -1;
            if (isArrayElement(code.getForth())) {
                index = getIndex(code.getForth());
            }
            System.out.println(symbolTable.getSymbolValue(code.getForth(), index));
        }
        if (instype.equals(FourCode.IN)) {
            mLevel++;
        }
        if (instype.equals(FourCode.OUT)) {
            symbolTable.deregister(mLevel);
            mLevel--;
        }
        if (instype.equals(FourCode.INT)) {
            if (code.getThird() != null) {
                Symbol symbol = new Symbol(code.getForth(), Symbol.ARRAY_INT, mLevel);
                symbol.getValue().initArray(getInt(code.getThird()));
                symbolTable.register(symbol);
            } else {
                int intvalue = 0;
                if (code.getSecond() != null) {
                    intvalue = getInt(code.getSecond());
                }
                Symbol symbol = new Symbol(code.getForth(), Symbol.SINGLE_INT, mLevel, intvalue);
                symbolTable.register(symbol);
            }
        }
        if (instype.equals(FourCode.REAL)) {
            if (code.getThird() != null) {
                Symbol symbol = new Symbol(code.getForth(), Symbol.ARRAY_REAL, mLevel);
                symbol.getValue().initArray(getInt(code.getThird()));
                symbolTable.register(symbol);
            } else {
                double doublevalue = 0;
                if (code.getSecond() != null) {
                    doublevalue = getDouble(code.getSecond());
                }
                Symbol symbol = new Symbol(code.getForth(), Symbol.SINGLE_REAL, mLevel, doublevalue);
                symbolTable.register(symbol);
            }
        }
        if (instype.equals(FourCode.ASSIGN)) {
            Value value = getValue(code.getSecond());
            setValue(code.getForth(), value);
        }
        if (instype.equals(FourCode.PLUS)) {
            setValue(code.getForth(), getValue(code.getSecond()).PLUS(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.MINUS)) {
            if (code.getThird() != null) {
                setValue(code.getForth(), getValue(code.getSecond()).MINUS(getValue(code.getThird())));
            } else {
                setValue(code.getForth(), Value.NOT(getValue(code.getSecond())));
            }
        }
        if (instype.equals(FourCode.MUL)) {
            setValue(code.getForth(), getValue(code.getSecond()).MUL(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.DIV)) {
            setValue(code.getForth(), getValue(code.getSecond()).DIV(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.GT)) {
            setValue(code.getForth(), getValue(code.getSecond()).GT(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.LT)) {
            setValue(code.getForth(), getValue(code.getSecond()).LT(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.EQ)) {
            setValue(code.getForth(), getValue(code.getSecond()).EQ(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.GET)) {
            setValue(code.getForth(), getValue(code.getSecond()).GET(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.LET)) {
            setValue(code.getForth(), getValue(code.getSecond()).LET(getValue(code.getThird())));
        }
        if (instype.equals(FourCode.NEQ)) {
            setValue(code.getForth(), getValue(code.getSecond()).NEQ(getValue(code.getThird())));
        }
        pc++;//正常+1,需要jmp时不要执行到这里,务必直接return
    }
    
    
    private Value getValue(String id) throws InterpretException {
        if (id.matches("\\d*\\.\\d*")) {
            Value value = new Value(Symbol.SINGLE_REAL);
            value.setReal(Double.parseDouble(id));
            return value;
        }
        if (id.matches("\\d+")) {
            Value value = new Value(Symbol.SINGLE_INT);
            value.setInt(Integer.parseInt(id));
            return value;
        }
        int index = -1;
        if (isArrayElement(id)) {
            index = getIndex(id);
        }
        return symbolTable.getSymbolValue(getId(id), index);
    }
    
    /**
     * 给xx[xx]或者xx赋值
     * @param id
     * @param value
     * @throws InterpretException
     */
    private void setValue(String id, Value value) throws InterpretException {
        int index = -1;
        if (isArrayElement(id)) {
            index = getIndex(id);
        }
        int type = symbolTable.getSymbolType(getId(id));
        switch (type) {
        case Symbol.SINGLE_INT:
        case Symbol.SINGLE_REAL:
        {
            if (type == Symbol.SINGLE_REAL) {
                symbolTable.setSymbolValue(getId(id), value.toReal());
            } else {
                if (value.getType() == Symbol.SINGLE_REAL) {
                    throw new InterpretException("表达式" + id + "与变量类型不匹配");
                } else {
                    symbolTable.setSymbolValue(getId(id), value);
                }
            }
            break;
        }
        case Symbol.ARRAY_INT:
        case Symbol.ARRAY_REAL:
        {
            if (symbolTable.getSymbolValue(getId(id), index).getType() == Symbol.SINGLE_REAL) {
                symbolTable.setSymbolValue(getId(id), value.toReal().getReal(), index);
            } else {
                if (value.getType() == Symbol.SINGLE_REAL) {
                    throw new InterpretException("表达式 <" + id + "> 与变量类型不匹配");
                } else {
                    symbolTable.setSymbolValue(getId(id), value.getInt(), index);
                }
            }
            break;
        }
        case Symbol.TEMP:
            symbolTable.setSymbolValue(getId(id), value);
            break;
        default:
            break;
        }
    }
    
    /**
     * 判断是否是数组
     * @param id
     * @return
     */
    private boolean isArrayElement(String id) {
        return id.contains("[");
    }
    
    /**
     * 将用户输入的数据转为Value
     * @param str
     * @return
     * @throws InterpretException
     */
    private Value parseValue(String str) throws InterpretException {
        if (str.matches("[-+]?[0-9]*\\.?[0-9]+")) {
            Value value = new Value(Symbol.SINGLE_REAL);
            value.setReal(Double.parseDouble(str));
            return value;
        }
        if (str.matches("^(-?\\d+)$")) {
            Value value = new Value(Symbol.SINGLE_INT);
            value.setInt(Integer.parseInt(str));
            return value;
        }
        throw new InterpretException("输入非法");
    }
    
    /**
     * 传入形如 xx[xx],获取其中的索引值
     * @param id
     * @return
     * @throws InterpretException
     */
    private int getIndex(String id) throws InterpretException {
        String indexstr = id.substring(id.indexOf("[") + 1, id.length() - 1) + "";
        return getInt(indexstr);
    }
    
    /**
     * 传入一个字面值或者标识符,获取对应int值
     * @param value
     * @return
     * @throws InterpretException
     */
    private int getInt(String value) throws InterpretException {
        if (value.matches("^(-?\\d+)$")) {
            return Integer.parseInt(value);
        }
        Value valueint = symbolTable.getSymbolValue(value);
        if (valueint.getType() == Symbol.SINGLE_INT) {
            return valueint.getInt();
        } else {
            throw new InterpretException("不是整数");
        }
    }
    
    /**
     * 传入一个字面值或者标识符,获取对应double值
     * @param value
     * @return
     * @throws InterpretException
     */
    private double getDouble(String value) throws InterpretException {
        if (value.matches("^(-?\\d+)(\\.\\d+)?$")) {
            return Double.parseDouble(value);
        }
        Value valueint = symbolTable.getSymbolValue(value);
        return valueint.toReal().getReal();
    }
    
    /**
     * 传入形如xx[xx]或者xx 获取前面的id
     * @param id
     * @return
     */
    private String getId(String id) {
        if (isArrayElement(id)) {
            return id.substring(0, id.indexOf("[")) + "";//prevent from memory leak
        }
        return id;
    }
    
    //===================================================================
    //
    //  TreeNode解释器核心,不需要生成中间代码,弃用,存在bug
    //
    //===================================================================
/***********************************************************************
    private void interpret(TreeNode node) throws InterpretException {
        while (true) {
            switch (node.getType()) {
            case TreeNode.IF_STMT:
                interpretIfStmt(node);
                break;
            case TreeNode.WHILE_STMT:
                while (interpretExp(node.getLeft()).getType() == Symbol.TRUE) {
                    mLevel++;
                    interpret(node.getMiddle());
                    SymbolTable.getSymbolTable().deregister(mLevel);
                    mLevel--;
                }
                break;
            case TreeNode.READ_STMT:
            {
                SymbolTable table = SymbolTable.getSymbolTable();
                Scanner sc = new Scanner(System.in);
                String input = sc.next();
                if (table.getSymbolType(node.getLeft().getValue()) == Symbol.SINGLE_INT) {
                    Value value = new Value(Symbol.SINGLE_INT);
                    value.setInt(Integer.parseInt(input));
                    table.setSymbolValue(node.getLeft().getValue(), value);
                } else if (table.getSymbolType(node.getLeft().getValue()) == Symbol.SINGLE_REAL) {
                    Value value = new Value(Symbol.SINGLE_REAL);
                    value.setReal(Double.parseDouble(input));
                    table.setSymbolValue(node.getLeft().getValue(), value);
                } else if (table.getSymbolType(node.getLeft().getValue()) == Symbol.ARRAY_INT) {
                    table.setSymbolValue(node.getLeft().getValue(), Integer.parseInt(input), interpretExp(node.getLeft().getLeft()).getInt());
                } else if (table.getSymbolType(node.getLeft().getValue()) == Symbol.ARRAY_REAL) {
                    table.setSymbolValue(node.getLeft().getValue(), Double.parseDouble(input), interpretExp(node.getLeft().getLeft()).getInt());
                } else {
                    throw new InterpretException("输入与变量类型不匹配");
                }
                break;
            }
            case TreeNode.WRITE_STMT:
                System.out.println(interpretExp(node.getLeft()));
                break;
            case TreeNode.DECLARE_STMT:
            {
                SymbolTable table = SymbolTable.getSymbolTable();
                TreeNode var = node.getLeft();
                if (var.getLeft() == null) {//单值
                    Value value = null;
                    if (node.getMiddle() != null) {
                        value = interpretExp(node.getMiddle());
                    }
                    if (var.getDataType() == Token.INT) {
                        int intvalue = 0;
                        if (value != null) {
                            if (value.getType() == Symbol.SINGLE_INT) {
                                intvalue = value.getInt();
                            } else {
                                throw new InterpretException("表达式与变量类型不匹配");
                            }
                        }
                        Symbol symbol = new Symbol(var.getValue(), Symbol.SINGLE_INT, mLevel, intvalue);
                        table.register(symbol);
                    } else if (var.getDataType() == Token.REAL) {
                        double realvalue = 0;
                        if (value != null) {
                            if (value.getType() == Symbol.SINGLE_REAL) {
                                realvalue = value.getReal();
                            } else {
                                realvalue = value.getInt();
                            }
                        }
                        Symbol symbol = new Symbol(var.getValue(), Symbol.SINGLE_REAL, mLevel, realvalue);
                        table.register(symbol);
                    }
                } else {
                    int len = interpretExp(var.getLeft()).getInt();
                    if (var.getDataType() == Token.INT) {
                        Symbol symbol = new Symbol(var.getValue(), Symbol.ARRAY_INT, mLevel);
                        symbol.getValue().initArray(len);
                        table.register(symbol);
                    } else {
                        Symbol symbol = new Symbol(var.getValue(), Symbol.ARRAY_REAL, mLevel);
                        symbol.getValue().initArray(len);
                        table.register(symbol);
                    }
                }
                break;
            }
            case TreeNode.ASSIGN_STMT:
            {
                SymbolTable table = SymbolTable.getSymbolTable();
                Value value = interpretExp(node.getMiddle());
                TreeNode var = node.getLeft();
                if (var.getLeft() == null) {//单值
                    if (table.getSymbolValue(var.getValue()).getType() == Symbol.SINGLE_REAL) {
                        table.setSymbolValue(var.getValue(), value.toReal());
                    } else {
                        if (value.getType() == Symbol.SINGLE_REAL) {
                            throw new InterpretException("表达式与变量类型不匹配");
                        } else {
                            table.setSymbolValue(var.getValue(), value);
                        }
                    }
                } else {
                    int index = interpretExp(var.getLeft()).getInt();
                    if (table.getSymbolValue(var.getValue(), index).getType() == Symbol.SINGLE_REAL) {
                        table.setSymbolValue(var.getValue(), value.toReal().getReal(), index);
                    } else {
                        if (value.getType() == Symbol.SINGLE_REAL) {
                            throw new InterpretException("表达式与变量类型不匹配");
                        } else {
                            table.setSymbolValue(var.getValue(), value.getInt(), index);
                        }
                    }
                }
                break;
            }
            default:
                break;
            }
            if (node.getNext() != null) {
                node = node.getNext();
            } else {
                break;
            }
        }
    }
    
    private void interpretIfStmt(TreeNode node) throws InterpretException {
        if (node.getType() == TreeNode.IF_STMT) {
            if (interpretExp(node.getLeft()).getType() == Symbol.TRUE) {
                mLevel++;
                interpret(node.getMiddle());
                SymbolTable.getSymbolTable().deregister(mLevel);
                mLevel--;
            } else if (node.getRight() != null) {
                mLevel++;
                interpret(node.getRight());
                SymbolTable.getSymbolTable().deregister(mLevel);
                mLevel--;
            }
        }
    }
    
    private Value interpretExp(TreeNode node) throws InterpretException {
        if (node.getType() == TreeNode.EXP) {
            switch (node.getDataType()) {
            case Token.LOGIC_EXP:
                return interpretLogicExp(node);
            case Token.ADDTIVE_EXP:
                return interpretAddtiveExp(node);
            case Token.TERM_EXP:
                return interpretTermExp(node);
            default:
                throw new InterpretException("复合表达式非法");
            }
        } else if (node.getType() == TreeNode.FACTOR) {
            if (node.getDataType() == Token.MINUS) {
                return Value.NOT(interpretExp(node.getLeft()));
            } else {
                return interpretExp(node.getLeft());
            }
        } else if (node.getType() == TreeNode.VAR) {
            if (node.getLeft() == null) {//单值
                return SymbolTable.getSymbolTable().getSymbolValue(node.getValue());
            } else {
                Value index = interpretExp(node.getLeft());
                if (index.getType() != Symbol.SINGLE_INT) {
                    throw new InterpretException("数组下标不是整数");
                }
                return SymbolTable.getSymbolTable().getSymbolValue(node.getValue(), index.getInt());
            }
        } else if (node.getType() == TreeNode.LITREAL) {
            if (node.getDataType() == Token.LITERAL_INT) {
                Value rv = new Value(Symbol.SINGLE_INT);
                rv.setInt(Integer.parseInt(node.getValue()));
                return rv;
            } else {
                Value rv = new Value(Symbol.SINGLE_REAL);
                rv.setReal(Double.parseDouble(node.getValue()));
                return rv;
            }
        }
        throw new InterpretException("表达式非法");
    }
    
    private Value interpretLogicExp(TreeNode node) throws InterpretException {
        switch (node.getMiddle().getDataType()) {
        case Token.GT:
            return interpretExp(node.getLeft()).GT(interpretExp(node.getRight()));
        case Token.GET:
            return interpretExp(node.getLeft()).GET(interpretExp(node.getRight()));
        case Token.LT:
            return interpretExp(node.getLeft()).LT(interpretExp(node.getRight()));
        case Token.LET:
            return interpretExp(node.getLeft()).LET(interpretExp(node.getRight()));
        case Token.EQ:
            return interpretExp(node.getLeft()).EQ(interpretExp(node.getRight()));
        case Token.NEQ:
            return interpretExp(node.getLeft()).NEQ(interpretExp(node.getRight()));
        default:
            throw new InterpretException("逻辑比较非法");
        }
    }
    
    private Value interpretAddtiveExp(TreeNode node) throws InterpretException {
        switch (node.getMiddle().getDataType()) {
        case Token.PLUS:
            return interpretExp(node.getLeft()).PLUS(interpretExp(node.getRight()));
        case Token.MINUS:
            return interpretExp(node.getLeft()).MINUS(interpretExp(node.getRight()));
        default:
            throw new InterpretException("算数运算非法");
        }
    }
    
    private Value interpretTermExp(TreeNode node) throws InterpretException {
        switch (node.getMiddle().getDataType()) {
        case Token.MUL:
            return interpretExp(node.getLeft()).MUL(interpretExp(node.getRight()));
        case Token.DIV:
            return interpretExp(node.getLeft()).DIV(interpretExp(node.getRight()));
        default:
            throw new InterpretException("算数运算非法");
        }
    }
***********************************************************************/
}
