
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;

import ast.CianetoClass;
import ast.LiteralInt;
import ast.MetaobjectAnnotation;
import ast.Program;
import ast.Statement;
import lexer.Lexer;
import lexer.Token;

public class Compiler {

    // { foo } -- pode existir nenhum ou mais 'foo'
    // [ foo ] -- 'foo' eh opcional
    // 'foo' -- palavras entre '' sao terminais (palavra reservada)
    // next(); = lexer.nextToken();

    // compile must receive an input with an character less than
    // p_input.lenght
    public Program compile(char[] input, PrintWriter outError) {

        ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
        signalError = new ErrorSignaller(outError, compilationErrorList);
        symbolTable = new SymbolTable();
        lexer = new Lexer(input, signalError);
        signalError.setLexer(lexer);

        Program program = null;
        lexer.nextToken();
        program = program(compilationErrorList);
        return program;
    }

    // program ::= { annot } classDec { { annot } classDec }
    private Program program(ArrayList<CompilationError> compilationErrorList) {
        // Program ::= CianetoClass { CianetoClass }
        ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
        ArrayList<CianetoClass> CianetoClassList = new ArrayList<>();
        Program program = new Program(CianetoClassList, metaobjectCallList, compilationErrorList);
        boolean thereWasAnError = false;

        // Aqui comeca -- ({ annot } classDec { { annot } classDec })
        // Neste while temos declaracao de classes, podemos ter uma ou varias classes declaradas
        while (lexer.token == Token.CLASS ||
                (lexer.token == Token.ID && lexer.getStringValue().equals("open")) ||
                lexer.token == Token.ANNOT) {
            try {
                // Aqui -- { annot }, nenhuma anotacao ou varias
                while (lexer.token == Token.ANNOT) {
                    annot(metaobjectCallList);
                }

                // Chamando classDec
                classDec();
            } catch (CompilerError e) {
                // if there was an exception, there is a compilation error
                thereWasAnError = true;
                while (lexer.token != Token.CLASS && lexer.token != Token.EOF) {
                    try {
                        next();
                    } catch (RuntimeException ee) {
                        e.printStackTrace();
                        return program;
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                thereWasAnError = true;
            }

        }
        if (!thereWasAnError && lexer.token != Token.EOF) {
            try {
                error("End of file expected");
            } catch (CompilerError e) {
            }
        }
        return program;
    }

    /**
     * parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     *
     * @cep(5, "'class' expected") <br>
     * class Program <br>
     * func run { } <br>
     * end <br>
     * </code>
     */
    @SuppressWarnings("incomplete-switch")
    // annot ::= '@' id [ '(' { annotParam } ')' ]
    private void annot(ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
        // '@' ja consumido na chamada anterior
        // id = 'nce' ou 'cep'
        String id = lexer.getMetaobjectName();
        int lineNumber = lexer.getLineNumber();

        next();

        ArrayList<Object> metaobjectParamList = new ArrayList<>();
        boolean getNextToken = false;

        // Caso encontre um '(' -> '(' { annotParam } ')'
        if (lexer.token == Token.LEFTPAR) {
            // metaobject call with parameters
            next();

            // nenhuma anotacao ou varias
            // annotParam ::= LITERALINT | LITERALSTRING | ID
            while (lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING ||
                    lexer.token == Token.ID) {
                switch (lexer.token) {
                    case LITERALINT:
                        metaobjectParamList.add(lexer.getNumberValue());
                        break;
                    case LITERALSTRING:
                        metaobjectParamList.add(lexer.getLiteralStringValue());
                        break;
                    case ID:
                        metaobjectParamList.add(lexer.getStringValue());
                }

                next();

                // Caso encontre uma virgula, continua o while
                if (lexer.token == Token.COMMA) {
                    next();
                }
                else {
                    break;
                }
            }

            // Se depois que ler o annotParam nao encontrar ')', lanca um erro
            if (lexer.token != Token.RIGHTPAR) {
                error("')' expected after metaobject call with parameters");
            }
            else {
                getNextToken = true;
            }
        }

        // Parte ja implementada pelo professor
        if (id.equals("nce")) {
            if (metaobjectParamList.size() != 0)
                error("Metaobject 'nce' does not take parameters");
        } else if (id.equals("cep")) {
            if (metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4)
                error("Metaobject 'cep' take three or four parameters");
            if (!(metaobjectParamList.get(0) instanceof Integer)) {
                error("The first parameter of metaobject 'cep' should be an integer number");
            } else {
                int ln = (Integer) metaobjectParamList.get(0);
                metaobjectParamList.set(0, ln + lineNumber);
            }
            if (!(metaobjectParamList.get(1) instanceof String) || !(metaobjectParamList.get(2) instanceof String))
                error("The second and third parameters of metaobject 'cep' should be literal strings");
            if (metaobjectParamList.size() >= 4 && !(metaobjectParamList.get(3) instanceof String))
                error("The fourth parameter of metaobject 'cep' should be a literal string");

        }
        metaobjectAnnotationList.add(new MetaobjectAnnotation(id, metaobjectParamList));

        // Se encontrou um ')' no final, anda o token
        if (getNextToken) lexer.nextToken();
    }

    // classDec ::= [ 'open' ] 'class' id [ 'extends' id ] memberList
    private void classDec() {
        if (lexer.token == Token.ID && lexer.getStringValue().equals("open")) {
            // open class
        }

        // Se nao encontrar a palavra 'class', lanca um erro
        if (lexer.token != Token.CLASS) {
            error("'class' expected");
        }

        next();

        // Se nao encontrar um ID depois de 'class', lanca um erro
        if (lexer.token != Token.ID){
            error("Identifier expected");
        }

        // TODO: adicionar a classe declarada na tabela (analise semantica)
        // Recuperando o ID da classe (nome da classe)
        String className = lexer.getStringValue();
        next();

        // Se encontrar a palavra 'extends' apos o ID
        if (lexer.token == Token.EXTENDS) {
            next();

            // Precisa existir um outro ID (uma classe herda de outra classe), se nao encontrar lanca um erro
            if (lexer.token != Token.ID){
                error("Identifier expected");
            }

            // Recuperando o ID da super classe
            String superclassName = lexer.getStringValue();
            next();
        }

        // Chamando memberList
        memberList();

        // Se nao encontrar a palavra 'end', lanca um erro
        if (lexer.token != Token.END) {
            error("'end' expected");
        }

        next();
    }

    // memberList ::= { [ qualifier ] member }
    //                  Se não entrar em nada no qualifier, assume que o metodo ou atributo eh publico
    private void memberList() {
        // Enquanto houver um metodo
        while (true) {
            // Verifica se o metodo eh public, private, override, etc..
            qualifier();

            // Se nao encontrou nada no qualifier, pode ser uma variavel ou uma funcao ou nada
            if (lexer.token == Token.VAR) {
                fieldDec();
            } else if (lexer.token == Token.FUNC) {
                methodDec();
            } else {
                break;
            }
        }
    }

    private void error(String msg) {
        this.signalError.showError(msg);
    }

    private void next() {
        lexer.nextToken();
    }

    private void check(Token shouldBe, String msg) {
        if (lexer.token != shouldBe) {
            error(msg);
        }
    }

    // methodDec ::=    'func' IdColon FormalParamDec [ '->' Type ] '{' StatementList '}' |
    //                  'func' Id [ '->' Type ] '{' StatementList '}'
    private void methodDec() {
        // TODO: Adicionar o Id encontrar na tabela hash (Semantica)
        next(); // Ja verificou se tinha 'func' na chamada anterior

        // Se for apenas ID, o metodo nao possui parametros
        if (lexer.token == Token.ID) {
            // unary method
            next();

        }
        // Se nao pode ser um metodo ('Identifier:') com parametros
        else if (lexer.token == Token.IDCOLON) {
            next();
            formalParamDec();
            // keyword method. It has parameters
        } else {
            error("An identifier or identifer: was expected after 'func'");
        }

        // Se encontrar '->', o metodo retorna alguma coisa
        if (lexer.token == Token.MINUS_GT) {
            // method declared a return type
            next();
            type();
        }

        // Verifica se o token eh diferente '{'
        if (lexer.token != Token.LEFTCURBRACKET) {
            error("'{' expected");
        }

        next();
        statementList();

        // Verifica se o token eh diferente '}'
        if (lexer.token != Token.RIGHTCURBRACKET) {
            error("'{' expected");
        }

        next();
    }

    // formalParamDec ::= paramDec { ',' ParamDec }
    private void formalParamDec() {
        // TODO: Adicionar na tabela para analise semantica
        paramDec(); // Chama paramDec

        // Enquanto tiver parametros, chama o paramDec
        while (lexer.token == Token.COMMA) {
            next(); // Consome a ','
            paramDec();
        }
    }

    // paramDec ::= Type Id
    private void paramDec() {
        // TODO: Adicionar na tabela para analise semantica (o tipo do id)
        type();

        // Se nao encontrar um identificador apos o tipo de uma variavel, lanca um erro
        if (lexer.token != Token.ID) {
            error("A variable name was expected");
        }

        next(); // Consome o id
    }

    // statementList ::= { Statement }
    private void statementList() {
        // only '}' is necessary in this test
        // Continua chamando o statement, se for diferente de '}' e 'end'
        // Em outras palavras, continua

        // FORNECIDO PELO PROFESSOR
//        while (lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
//              statement();
//         }

        // Continua chamando o statement, se for diferente de '}'
        // Em outras palavras, continua montando o statement ate chegar no fim do metodo
        while (lexer.token != Token.RIGHTCURBRACKET) {
            statement();
        }
    }

    // statement ::=    AssignExpr ';' | IfStat | WhileStat | ReturnStat ';' |
    //                  WriteStat ';' | 'break' ';' | ';' |
    //                  RepeatStat ';' | LocalDec ';' |
    //                  AssertStat ';'
    private void statement() {
        boolean checkSemiColon = true;

        // Verifica qual dos statement irá chamar
        switch (lexer.token) {
            case IF:
                ifStat();
                checkSemiColon = false;
                break;
            case WHILE:
                whileStat();
                checkSemiColon = false;
                break;
            case RETURN:
                returnStat();
                break;
            case BREAK:
                breakStat();
                break;
            case SEMICOLON:
                next();
                break;
            case REPEAT:
                repeatStat();
                break;
            case VAR:
                localDec();
                break;
            case ASSERT:
                assertStat();
                break;
            default:

                // Se for um Id e o Id for igual a 'Out', chama o writeStat
                if (lexer.token == Token.ID && lexer.getStringValue().equals("Out")) {
                    writeStat();
                }
                // Se nao chama o Assign
                else {
                    assignExpr();
                }
        }

        // Se nao for ifStat nem whileStat, nao verifica se tem ';'
        if (checkSemiColon) {
            check(Token.SEMICOLON, "';' expected");
        }
    }

    // localDec ::= 'var' Type IdList [ '=' Expression ] [';']
    private void localDec() {
        // TODO: verificar se a variavel local ja foi declarada no escopo
        // TODO: verificar se a atribuicao eh para apenas uma variavel
        // TODO: verificar se o retorno eh compativel para a variavel
        next(); // le token 'var'
        type();
        check(Token.ID, "A variable name was expected");
        while (lexer.token == Token.ID) {
            next();
            if (lexer.token == Token.COMMA) {
                next();
            } else {
                break;
            }
        }

        if (lexer.token == Token.ASSIGN) {
            next();
            // check if there is just one variable
            expr();
        }

        // ';' eh opcional
        if (lexer.token == Token.SEMICOLON){
            next(); // Como ';' eh final, anda o token
        }

    }

    // repeatStat ::= 'repeat' StatementList 'until' Expression
    private void repeatStat() {
        // TODO: Verificar se expr é Boolean
        next(); // le token 'repeat'

        // Anotações do professor
//        while (lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
//            statement();
//        }

        statementList();

        check(Token.UNTIL, "'until' was expected");
        next(); // le token 'until'

        expr();
    }

    // breakStat ::= 'break'
    private void breakStat() {
        next(); // le o token 'break'
    }

    // returnStat ::= 'return' Expression
    private void returnStat() {
        next(); // le o token 'return'
        expr();
    }

    // whileStat ::= 'while' Expression '{' StatementList '}'
    private void whileStat() {
        // TODO: Verificar se expr é Boolean
        next(); // le o token 'while'
        expr();

        check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
        next();

        statementList();

        // Anotações do professor
//        while (lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
//            statement();
//      }

        check(Token.RIGHTCURBRACKET, "'}' was expected");
    }

    //ifStat ::= 'if' Expression '{' Statement '}' [ 'else' '{' Statement '}' ]
    private void ifStat() {
        // TODO: Verificar se expr é Boolean
        next(); // le o token 'if'
        expr();

        check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
        next();

        statement();
        check(Token.RIGHTCURBRACKET, "'}' was expected");
        next();

//        while (lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE) {
//            statement();
//        }

        if (lexer.token == Token.ELSE) {
            next();
            check(Token.LEFTCURBRACKET, "'{' expected after 'else'");

            next();
            statement();

            check(Token.RIGHTCURBRACKET, "'}' was expected");
        }
    }

    // writeStat ::= 'Out' '.' [ 'print:' | 'println:' ] Expression (?)
    private void writeStat() {
        next(); // le o token 'Out'
        check(Token.DOT, "a '.' was expected after 'Out'");

        next();
        check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");

        String printName = lexer.getStringValue();
        if (!printName.equals("print:") || !printName.equals("println:")) {
            this.error("'print:' or 'println:' was expected after 'Out.'");
        }

        expr();
    }

    // assignExpr ::= Expression [ '=' Expression ]
    private void assignExpr(){
        // TODO: Ver se a atribuicao eh valida (int recebe int) (float recebe float) etc...
        expr();

        // Se encontrar um '='
        if (lexer.token == Token.ASSIGN) {
            next(); // Le o token '='
            expr();
        }
    }

    // expr ::= SimpleExpression [ Relation SimpleExpression ]
    private void expr() {
        simpleExpr();

        // Se for uma expressao de relacao '==' | '<' | '>' | '<=' ...
        if (relation()) {
            next();
            simpleExpr();
        }
    }

    // Relation ::= '==' | '<' | '>' | '<=' | '>=' | '!='
    private boolean relation() {
        if (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT ||
            lexer.token == Token.LE || lexer.token == Token.GE || lexer.token == Token.NEQ) {
            return true;
        }

        return false;
    }

    // simpleExpr ::= SumSubExpression { '++' SumSubExpression }
    private void simpleExpr() {
        sumSubExpr();

        // Se encontrou '+'
        // TODO: Verificar se o next() consome '+' ou '++'
        if (lexer.token == Token.PLUS) {
            next(); // Consome o primeiro '+'
//            next(); // Consome o segundo

            sumSubExpr();
        }
    }

    // sumSubExpr ::= Term { LowOperator Term }
    private void sumSubExpr() {
        term();

        // Enquanto encontrar '+' | '-' | '||' continua no while
        while (lowOperator()) {
            next(); // Consome o simbolo
            term();
        }
    }

    private boolean lowOperator() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
            return true;
        }

        return false;
    }

    // term ::= SignalFactor { HighOperator SignalFactor }
    private void term() {
        signalFactor();

        // Enquanto encontrar '*' | '/' | '&&' continua no while
        while (highOperator()) {
            next(); // Consome o simbolo
            signalFactor();
        }
    }

    private boolean highOperator() {
        if (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
            return true;
        }

        return false;
    }

    // signalFactor ::= [ Signal ] Factor
    private void signalFactor() {
        // Pode encontrar '+' | '-', se encontrar
        if (signal()) {
            next(); // Consome o simbolo
        }

        factor();
    }

    private boolean signal() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
            return true;
        }

        return false;
    }

    // factor ::= BasicValue | '(' Expression ')' | '!' Factor | 'nil' | ObjectCreation | PrimaryExpr
    private void factor() {
        // TODO: BasicValue eh variavel do tipo INT ou INTLITERAL, STRING OU STRINGLITERAL  ?
        switch (lexer.token) {
            case INT:
                basicValue();
                break;
            case LITERALINT:
                basicValue();
                break;
            case BOOLEAN:
                basicValue();
                break;
            case STRING:
                basicValue();
                break;
            case LITERALSTRING:
                basicValue();
                break;
            case LEFTPAR:
                next(); // Consome o '('
                expr();

                check(Token.RIGHTPAR, "')' expected");
                next(); // Consome o ')'
                break;
            case NOT:
                factor();
                break;
            case NULL:
                next(); // Le o 'nil'
                break;
            default:

                // TODO: Verificar como identificar qual metodo chamar objectCreation | primaryExpr
//                if (lexer.token == Token.ID) {
//                    objectCreation();
//                } else if (lexer) {
//
//                }
        }
    }

    // basicValue ::= IntValue | BooleanValue | StringValue
    private void basicValue() {
        next();
    }

    // objectCreation ::= Id '.' 'new'
    private void objectCreation() {
    }

    // primaryExpr ::= 'super' '.' IdColon ExpressionList | 'super' '.' Id | Id | Id '.' Id | Id '.' IdColon ExpressionList | 'self' | 'self' '.' Id |
                    // 'self' '.' IdColon ExpressionList | 'self' '.' Id '.' IdColon ExpressionList | 'self' '.' Id '.' Id | ReadExpr
    private void primaryExpr() {
    }

    // exprList ::= Expression { ',' Expression }
    private void exprList() {
    }

    // readExpr ::= 'In' '.' [ 'readInt' | 'readString' ]
    private void readExpr() {
    }

    // fieldDec ::= 'var' Type IdList ';'
    private void fieldDec() {
        // TODO: Adicionar o Id encontrar na tabela hash (Semantica)
        next(); // Ja verificou se tinha 'var' na chamada anterior
        type(); // Verificado

        // Se nao encontrar um Id depois do tipo, lanca um erro
        if (lexer.token != Token.ID) {
            this.error("A variable name was expected");
        } else {
            // Enquanto existirem mais Id
            while (lexer.token == Token.ID) {
                next(); // Anda o token

                // Ve se encontrou uma virgula
                if (lexer.token == Token.COMMA) {
                    next(); // Anda o token

                // O ';' eh opcional, portanto se encontra-lo ou nao para o laco
                } else if (lexer.token == Token.SEMICOLON){
                    next(); // Como ';' eh final, anda o token
                    break;
                } else {
                    break;
                }
            }
        }

    }

    // type ::= BasicType | Id
    private void type() {
        // BasicType ::= 'Int' | 'Boolean' | 'String'
        if (lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING) {
            // TODO: Adicionar o tipo da variavel na tabela para analise semantica
            String idType = lexer.getStringValue();
            next();
        }
        else if (lexer.token == Token.ID) {
            // TODO: Verifica se o id existe (classe)
            next();
        }
        // Lança um erro se não for ID nem um tipo basico
        else {
            this.error("A type was expected");
        }

    }

    // qualifier ::=    'private' | 'public' | 'override' | 'override' 'public' |
    //                  'final' | 'final' 'public' | 'final' 'override' |
    //                  'final' 'override' 'public'
    private void qualifier() {
        // Parte ja implementada pelo professor
        if (lexer.token == Token.PRIVATE) { // private
            next();
        } else if (lexer.token == Token.PUBLIC) { // public
            next();
        } else if (lexer.token == Token.OVERRIDE) { // override
            next();
            if (lexer.token == Token.PUBLIC) { // override public
                next();
            }
        } else if (lexer.token == Token.FINAL) { // final
            next();
            if (lexer.token == Token.PUBLIC) { // final public
                next();
            } else if (lexer.token == Token.OVERRIDE) { // final override
                next();
                if (lexer.token == Token.PUBLIC) { // final override public
                    next();
                }
            }
        }
    }

    /**
     * change this method to 'private'.
     * uncomment it
     * implement the methods it calls
     */
    // assertStat ::= 'assert' Expression ',' StringValue
    public Statement assertStat() {

        next();
        int lineNumber = lexer.getLineNumber();
        expr();
        if (lexer.token != Token.COMMA) {
            this.error("',' expected after the expression of the 'assert' statement");
        }
        lexer.nextToken();
        if (lexer.token != Token.LITERALSTRING) {
            this.error("A literal string expected after the ',' of the 'assert' statement");
        }
        String message = lexer.getLiteralStringValue();
        lexer.nextToken();
        if (lexer.token == Token.SEMICOLON)
            lexer.nextToken();

        return null;
    }


    private LiteralInt literalInt() {

        LiteralInt e = null;

        // the number value is stored in lexer.getToken().value as an object of
        // Integer.
        // Method intValue returns that value as an value of type int.
        int value = lexer.getNumberValue();
        lexer.nextToken();
        return new LiteralInt(value);
    }

    private static boolean startExpr(Token token) {

        return token == Token.FALSE || token == Token.TRUE
                || token == Token.NOT || token == Token.SELF
                || token == Token.LITERALINT || token == Token.SUPER
                || token == Token.LEFTPAR || token == Token.NULL
                || token == Token.ID || token == Token.LITERALSTRING;

    }

    private SymbolTable symbolTable;
    private Lexer lexer;
    private ErrorSignaller signalError;

}
