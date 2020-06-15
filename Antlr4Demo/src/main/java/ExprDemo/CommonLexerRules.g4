lexer grammar CommonLexerRules;

/**
    词法分析起点
**/
ID  :   [a-zA-Z]+ ; // 匹配标识符
INT :   [0-9]+ ; // 匹配整数
NEWLINE :    '\r'? '\n' ; // 告诉语法分析器从一个新行开始（语句的终止标志）
WS :    [ \t]+ -> skip ; // 丢弃空白字符