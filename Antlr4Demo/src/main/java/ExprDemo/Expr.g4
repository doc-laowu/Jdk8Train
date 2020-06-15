grammar Expr;
import CommonLexerRules; //导入词法规则文件

/**
    语法分析起点
**/
prog:   stat* ;

stat:   expr NEWLINE              # printExpr
    |   ID '=' expr NEWLINE      # assign
    |   NEWLINE                   # blank
    ;
expr:   expr op=(MUL | DIV) expr      # MulDiv
    |   expr op=(ADD | SUB) expr      # AddSub
    |   INT                       # int
    |   ID                        # id
    |   '(' expr ')'             # parens
    ;

MUL :   '*' ; // 为上述语法中使用'*'命名
DIV :   '/' ;
ADD :   '+' ;
SUB :   '-' ;