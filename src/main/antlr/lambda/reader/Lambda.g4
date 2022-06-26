grammar Lambda;

file:
    definition* top_level_expression? EOF;

expression
    : NAME # Name
    | '\\' NAME '.' body # Function
    | '(' expression expression+ ')' # Application
    ;

top_level_expression: expression+;

body: expression;

definition: 'def' NAME+ '=' top_level_expression '\n';

NAME: [a-zA-Z0-9_+\-<>]+;

WS
    : [ \t\r\n] -> skip
    ;



