grammar Lambda;

expression
    : name
    | function
    | application
    ;

name: NAME;

function: '\\' name '.' expression;

application: '(' expression expression ')';

NAME: [^a-zA-Z0-9_+\-<>]+;

WS
    : [ \t\r\n] -> skip
    ;



