package com.shaw.cmmjava.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.shaw.cmmjava.CodeGenerater;
import com.shaw.cmmjava.Util;
import com.shaw.cmmjava.exception.InterpretException;
import com.shaw.cmmjava.exception.ParserException;
import com.shaw.cmmjava.model.FourCode;
import com.shaw.cmmjava.model.Token;
import com.shaw.cmmjava.model.TreeNode;

public class Main {
    
    private static String filestr = null;
    private static boolean saved = false;
    
    public static void main(String[] args) {
        
        final Display display = new Display();
        final Shell shell = new Shell(display);
        final StyledText codedata = new StyledText(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        final JavaLineStyler lineStyler = new JavaLineStyler();
        final Text resultdata = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        shell.setSize(800,600);
        shell.setText("CMM解释器-shaw");
        //菜单开始
        Menu menu = new Menu(shell, SWT.BAR);
        MenuItem file = new MenuItem(menu, SWT.CASCADE);
        file.setText("文件(&F)");
        Menu filemenu = new Menu(shell, SWT.DROP_DOWN);
        file.setMenu(filemenu);
        MenuItem openItem = new MenuItem(filemenu, SWT.PUSH);
        openItem.setText("打开(&O)\tCtrl+O");
        openItem.setAccelerator(SWT.CTRL + 'O');
        MenuItem saveItem = new MenuItem(filemenu, SWT.PUSH);
        saveItem.setText("保存(&S)\tCtrl+S");
        saveItem.setAccelerator(SWT.CTRL + 'S');
        MenuItem saveasItem = new MenuItem(filemenu, SWT.PUSH);
        saveasItem.setText("另存为(&A)\tCtrl+Shift+S");
        saveasItem.setAccelerator(SWT.CTRL + SWT.SHIFT + 'S');
        new MenuItem(filemenu, SWT.SEPARATOR);
        MenuItem exitItem = new MenuItem(filemenu, SWT.PUSH);
        exitItem.setText("退出(&E)");
        MenuItem run = new MenuItem(menu, SWT.CASCADE);
        run.setText("解释(&C)");
        Menu runmenu = new Menu(shell, SWT.DROP_DOWN);
        run.setMenu(runmenu);
        MenuItem lexicalItem = new MenuItem(runmenu, SWT.PUSH);
        lexicalItem.setText("词法分析(&L)");
        MenuItem parseItem = new MenuItem(runmenu, SWT.PUSH);
        parseItem.setText("语法分析(&P)");
        MenuItem generateItem = new MenuItem(runmenu, SWT.PUSH);
        generateItem.setText("中间代码(&G)");
        MenuItem runItem = new MenuItem(runmenu, SWT.PUSH);
        runItem.setText("解释执行(&I)");
        MenuItem help = new MenuItem(menu, SWT.CASCADE);
        help.setText("帮助(&H)");
        Menu helpmenu = new Menu(shell, SWT.DROP_DOWN);
        help.setMenu(helpmenu);
        MenuItem aboutItem = new MenuItem(helpmenu, SWT.PUSH);
        aboutItem.setText("关于(&A)");
        shell.setMenuBar(menu);
        //菜单结束
        //菜单事件开始
        exitItem.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!shell.isDisposed()) {
                    display.dispose( );
                }
                return;
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        final String[] filterExt = {"*.cmm", "*.txt", "*.*"};
        SelectionListener openListener = new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(shell, SWT.OPEN);
                fd.setText("打开");
                fd.setFilterExtensions(filterExt);
                filestr = fd.open();
                if (filestr != null) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filestr), "UTF-8"));
                        final StringBuilder sb = new StringBuilder();
                        String content;
                        while ((content = br.readLine()) != null) {
                            sb.append(content);
                            sb.append(System.getProperty("line.separator"));
                        }
                        br.close();
                        Display display = codedata.getDisplay();
                        display.asyncExec(new Runnable() {
                            public void run() {
                                codedata.setText(sb.toString());
                                saved = true;
                            }
                        });
                        lineStyler.parseBlockComments(sb.toString());
                    } catch (FileNotFoundException e1) {
                        //e1.printStackTrace();
                    } catch (IOException e1) {
                        //e1.printStackTrace();
                    }
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        final SelectionListener saveListener = new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (filestr == null) {
                    FileDialog fd = new FileDialog(shell, SWT.SAVE);
                    fd.setText("保存");
                    fd.setFilterExtensions(filterExt);
                    filestr = fd.open()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ;
                }
                if (filestr != null) {
                    File file = new File(filestr);
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()) , "UTF-8"));
                        bw.write(codedata.getText());
                        bw.close();
                        saved = true;
                    } catch (IOException e1) {
                        //e1.printStackTrace();
                    }
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        final SelectionListener runListener = new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (saved == false) {
                        saveListener.widgetSelected(null);
                    }
                    if (saved == true) {
                        Process process = Runtime.getRuntime().exec("cmd /k start res" + File.separator + "interpret.bat " + filestr);
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }

            }


            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        openItem.addSelectionListener(openListener);
        saveItem.addSelectionListener(saveListener);
        saveasItem.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
              FileDialog fd = new FileDialog(shell, SWT.SAVE);
              fd.setText("另存为");
              fd.setFilterExtensions(filterExt);
              String filename = fd.open();
              if (filename != null) {
                  File file = new File(filename);
                  try {
                      if (!file.exists()) {
                          file.createNewFile();
                      }
                      FileWriter fw = new FileWriter(file.getAbsoluteFile());
                      BufferedWriter bw = new BufferedWriter(fw);
                      bw.write(codedata.getText());
                      bw.close();
                      fw.close();
                      filestr = filename;
                      saved = true;
                  } catch (IOException e1) {
                      //e1.printStackTrace();
                  }
              }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        lexicalItem.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saved == false) {
                    saveListener.widgetSelected(null);
                }
                if (saved == true) {
                    LinkedList<Token> tokenList;
                    try {
                        tokenList = Util.getTokenList(filestr);
                        resultdata.setText("");
                        for (Token token : tokenList) {
                            resultdata.append(token.toStringWithLine());
                            resultdata.append(System.getProperty("line.separator"));
                        }
                    } catch (IOException e1) {
                        resultdata.setText(e1.toString());
                        //e1.printStackTrace();
                    }
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        parseItem.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saved == false) {
                    saveListener.widgetSelected(null);
                }
                if (saved == true) {
                    try {
                        LinkedList<TreeNode> nodeList = Util.getNodeList(Util.getTokenList(filestr));
                        resultdata.setText("");
                        for (TreeNode node : nodeList) {
                            Util.printTreeNode(resultdata, node);
                        }
                    } catch (ParserException e1) {
                        resultdata.setText(e1.toString());
                        //e1.printStackTrace();
                    } catch (IOException e1) {
                        //e1.printStackTrace();
                    }
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        generateItem.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (saved == false) {
                        saveListener.widgetSelected(null);
                    }
                    if (saved == true) {
                        resultdata.setText("");
                        LinkedList<FourCode> codes = CodeGenerater.generateCode(filestr);
                        for (int i = 0; i<codes.size(); i++) {
                            resultdata.append( i + " : " + codes.get(i));
                            resultdata.append(System.getProperty("line.separator"));
                        }
                    }
                } catch (ParserException e1) {
                    resultdata.setText(e1.toString());
                    //e1.printStackTrace();
                } catch (InterpretException e1) {
                    resultdata.setText(e1.toString());
                    //e1.printStackTrace();
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        runItem.addSelectionListener(runListener);
        aboutItem.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append("CMM解释器" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                sb.append("作者: shaw" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                sb.append("介绍:" + System.getProperty("line.separator"));
                sb.append("该CMM解释器使用Java开发,目前可以在windows上运行,移植需要改动Main中的脚本调用和res文件夹中的脚本." + System.getProperty("line.separator") + System.getProperty("line.separator"));
                resultdata.setText(sb.toString());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        //菜单事件结束
        //工具栏开始
        ToolBar toolbar = new ToolBar(shell, SWT.HORIZONTAL);
        ToolItem openToolItem = new ToolItem(toolbar, SWT.PUSH);
        openToolItem.setImage(new Image(display, "res" + File.separator + "ic_open.png"));
        openToolItem.setToolTipText("打开文件");
        ToolItem saveToolItem = new ToolItem(toolbar, SWT.PUSH);
        saveToolItem.setImage(new Image(display, "res" + File.separator + "ic_save.png"));
        saveToolItem.setToolTipText("保存文件");
        ToolItem runToolItem = new ToolItem(toolbar, SWT.PUSH);
        runToolItem.setImage(new Image(display, "res" + File.separator + "ic_run.png"));
        runToolItem.setToolTipText("运行");
        toolbar.setSize(400,30);
        openToolItem.addSelectionListener(openListener);
        saveToolItem.addSelectionListener(saveListener);
        runToolItem.addSelectionListener(runListener);
        //工具栏结束
        //主要布局
        shell.setLayout(new FormLayout());
        shell.addShellListener (new ShellAdapter () {
            @Override
            public void shellClosed (ShellEvent e) {
                lineStyler.disposeColors();
                codedata.removeLineStyleListener(lineStyler);
            }
        });
        FormData fd = new FormData();
        fd.top = new FormAttachment(0, 24);
        fd.left = new FormAttachment(0, 1);
        fd.bottom = new FormAttachment(100,-1);
        fd.right = new FormAttachment(70,0);
        codedata.setLayoutData(fd);
        codedata.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                saved = false;
                lineStyler.parseBlockComments(codedata.getText());
            }
        });
        codedata.addLineStyleListener(lineStyler);
        fd = new FormData();
        fd.top = new FormAttachment(0, 24);
        fd.left = new FormAttachment(70, 3);
        fd.bottom = new FormAttachment(100,-1);
        fd.right = new FormAttachment(100,-1);
        resultdata.setLayoutData(fd);
        resultdata.setEditable(false);
        //主要布局结束
        
        shell.open();
        while(!shell.isDisposed()){
            if(!display.readAndDispatch( ))
                display.sleep( );
        }
        display.dispose( );

    }

}
