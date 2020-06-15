grammar Data;

file    :   group+ ;

group   :   INT  sequence[$INT.int] ;

sequence[int n]
locals [int i = 1;]
    :   ( {$i<=$n}? INT {$i++;} )* // 匹配N个整数
    ;

INT :   [0-9]+ ;
WS  :   [ \t\r\n]+ -> skip ;