grammar Lambda;

file:
    definition* top_level_expression? EOF;

expression
    : NAME # Name
    | '\\' NAME dot expression # Function
    | '(' expression expression ')' # Application
    ;

top_level_expression: expression+;

dot: '.';

definition: 'def' NAME equals top_level_expression '\n';

equals: '=';

NAME: [a-zA-Z0-9_+\-<>]+;

WS
    : [ \t\r\n] -> skip
    ;



