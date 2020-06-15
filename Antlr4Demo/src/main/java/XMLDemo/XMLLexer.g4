lexer grammar XMLLexer ;  // 孤岛语法 ： 处理相同文件中的不同格式

// 默认的“模式”：所有在标签之外的东西
OPEN        :   '<'                     ->      pushMode(INSIDE) ;
COMMENT     :   '<!--' .*? '-->'       ->      skip ;
EntityRef   :   '&' [a-z]+ ';' ;
TEXT        :   ~('<'|'&')+ ;  // 匹配任意除<和&之外的16位字符

// ----------------- 所有在标签之内的内容 ---------------------------
mode    INSIDE;
CLOSE       :   '>'                     ->      popMode ;  // 回到默认模式
SLASH_MODE  :   '/>'                    ->      popMode ;
EQUALS      :   '=' ;
STRING      :   '"' .*? '"' ;
SlashName   :   '/' Name ;
Name        :   ALPHA (ALPHA|DIGIT)* ;
S           :   [ \t\r\n]               ->      skip ;

fragment
ALPHA       :   [a-zA-Z] ;

fragment
DIGIT       :   [0-9] ;

