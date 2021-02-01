grammar Calculation;

root: expression;

expression: '(' expression ')' #Parentheses
    | left=expression operator='^' right=expression #Squared
    | left=expression operator='*' right=expression #Multiply
    | left=expression operator='/' right=expression #Divide
    | left=expression operator='+' right=expression #Plus
    | left=expression operator='-' right=expression #Minus
    | NUMBER #Number
    ;

NUMBER: [0-9]+('.'[0-9]+)?;
WHITESPACE: [ \t\r\n]+ -> skip;
