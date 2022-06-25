grammar Lambda;

file:
    definition* expression?;

expression
    : name
    | function
    | application
    ;

definition: 'def' name '=' expression;

name: NAME;

function: '\\' name '.' expression;

application: '(' expression expression ')';

NAME: [^a-zA-Z0-9_+\-<>]+;

WS
    : [ \t\r\n] -> skip
    ;



