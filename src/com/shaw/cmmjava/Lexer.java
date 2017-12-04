package com.shaw.cmmjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

import com.shaw.cmmjava.model.Token;

public class Lexer {
    private static BufferedReader mBufferedReader;
    private static int currentInt;
    private static char currentChar;
    private static int lineNo;
    
    public static LinkedList<Token> lexicalAnalyse(BufferedReader br) throws IOException {
        lineNo = 1;
        mBufferedReader = br;
        LinkedList<Token> tokenList = new LinkedList<Token>();
        StringBuilder sb = new StringBuilder();
        readChar();
        while(currentInt != -1) {
            //消耗空白字符
            if (currentChar == '\n'
                    || currentChar == '\r'
                    || currentChar == '\t'
                    || currentChar == '\f'
                    || currentChar == ' ') {
                readChar();
                continue;
            }
            //简单特殊符号
            switch (currentChar) {
            case ';':
                tokenList.add(new Token(Token.SEMI, lineNo));
                readChar();
                continue;
            case '+':
                tokenList.add(new Token(Token.PLUS, lineNo));
                readChar();
                continue;
            case '-':
                tokenList.add(new Token(Token.MINUS, lineNo));
                readChar();
                continue;
            case '*':
                tokenList.add(new Token(Token.MUL, lineNo));
                readChar();
                continue;
            case '(':
                tokenList.add(new Token(Token.LPARENT, lineNo));
                readChar();
                continue;
            case ')':
                tokenList.add(new Token(Token.RPARENT, lineNo));
                readChar();
                continue;
            case '[':
                tokenList.add(new Token(Token.LBRACKET, lineNo));
                readChar();
                continue;
            case ']':
                tokenList.add(new Token(Token.RBRACKET, lineNo));
                readChar();
                continue;
            case '{':
                tokenList.add(new Token(Token.LBRACE, lineNo));
                readChar();
                continue;
            case '}':
                tokenList.add(new Token(Token.RBRACE, lineNo));
                readChar();
                continue;
            case ',':
            	tokenList.add(new Token(Token.COMMA, lineNo));
                readChar();
                continue;
            // no default:
            }
            //复合特殊符号
            if (currentChar == '/') {
                readChar();
                if (currentChar == '*') {//多行注释
//                    tokenList.add(new Token(Token.LCOM, lineNo));
                    readChar();
                    while (true) {//使用死循环消耗多行注释内字符
                        if (currentChar == '*') {//如果是*,那么有可能是多行注释结束的地方
                            readChar();
                            if (currentChar == '/') {//多行注释结束符号
//                                tokenList.add(new Token(Token.RCOM, lineNo));
                                readChar();
                                break;
                            }
                        } else {//如果不是*就继续读下一个,相当于忽略了这个字符
                            readChar();
                        }
                    }
                    continue;
                } else if (currentChar == '/') {//单行注释
//                    tokenList.add(new Token(Token.SCOM, lineNo));
                    while (currentChar != '\n') {//消耗这一行之后的内容
                        readChar();
                    }
                    continue;
                } else {//是除号
                    tokenList.add(new Token(Token.DIV, lineNo));
                    continue;
                }
            } else if (currentChar == '=') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.EQ, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.ASSIGN, lineNo));
                }
                continue;
            } else if (currentChar == '>') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.GET, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.GT, lineNo));
                }
                continue;
            } else if (currentChar == '<') {
                readChar();
                if (currentChar == '=') {
                    tokenList.add(new Token(Token.LET, lineNo));
                    readChar();
                } else {
                    tokenList.add(new Token(Token.LT, lineNo));
                }
                continue;
            }else if(currentChar=='!'){
            	readChar();
            	if(currentChar=='='){
            		tokenList.add(new Token(Token.NEQ, lineNo));
            		readChar();
            	}
            }
            //数字
            if (currentChar >= '0' && currentChar <= '9') {
                boolean isReal = false;//是否小数
                while ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                    if (currentChar == '.') {
                        if (isReal) {
                            break;
                        } else {
                            isReal = true;
                        }
                    }
                    sb.append(currentChar);
                    readChar();
                }
                if (isReal) {
                    tokenList.add(new Token(Token.LITERAL_REAL, sb.toString(), lineNo));
                } else {
                    tokenList.add(new Token(Token.LITERAL_INT, sb.toString(), lineNo));
                }
                sb.delete(0, sb.length());
                continue;
            }
            //字符组成的标识符,包括保留字和ID
            if ((currentChar >= 'a' && currentChar <= 'z') 
            	|| (currentChar >= 'A' && currentChar <= 'Z')
            	|| currentChar == '_') {
                //取剩下的可能是的字符
                while ((currentChar >= 'a' && currentChar <= 'z')
                        || (currentChar >= 'A' && currentChar <= 'Z')
                        || currentChar == '_'
                        || (currentChar >= '0' && currentChar <= '9'))
                {
                    sb.append(currentChar);
                    readChar();
                }
                Token token = new Token(lineNo);
                String sbString = sb.toString();
                if (sbString.equals("if")) {
                    token.setType(Token.IF);
                } else if (sbString.equals("else")) {
                    token.setType(Token.ELSE);
                } else if (sbString.equals("while")) {
                    token.setType(Token.WHILE);
                } else if (sbString.equals("read")) {
                    token.setType(Token.READ);
                } else if (sbString.equals("write")) {
                    token.setType(Token.WRITE);
                } else if (sbString.equals("int")) {
                    token.setType(Token.INT);
                } else if (sbString.equals("double")) {
                    token.setType(Token.REAL);
                } else {
                    token.setType(Token.ID);
                    token.setValue(sbString);
                }
                sb.delete(0, sb.length());
                tokenList.add(token);
                continue;
            }
            readChar();
        }
        return tokenList;
    }
    
    /**
     * 调用此方法之后{@link Lexer#currentInt} 和 {@link Lexer#currentChar} 均会改变
     * 这个方法也会统计换行,但是方法本身不会改变字符流的读取
     * @throws IOException 
     */
    private static void readChar() throws IOException {
        currentChar = (char) (currentInt = mBufferedReader.read());
        if (currentChar == '\n') {
            lineNo++;
        }
    }
    
}
