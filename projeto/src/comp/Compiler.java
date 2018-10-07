
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

    private void methodDec() {
        lexer.nextToken();
        if (lexer.token == Token.ID) {
            // unary method
            lexer.nextToken();

        } else if (lexer.token == Token.IDCOLON) {
            // keyword method. It has parameters

        } else {
            error("An identifier or identifer: was expected after 'func'");
        }
        if (lexer.token == Token.MINUS_GT) {
            // method declared a return type
            lexer.nextToken();
            type();
        }
        if (lexer.token != Token.LEFTCURBRACKET) {
            error("'{' expected");
        }
        next();
        statementList();
        if (lexer.token != Token.RIGHTCURBRACKET) {
            error("'{' expected");
        }
        next();

    }

    private void statementList() {
        // only '}' is necessary in this test
        while (lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
            statement();
        }
    }

    private void statement() {
        boolean checkSemiColon = true;
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
                if (lexer.token == Token.ID && lexer.getStringValue().equals("Out")) {
                    writeStat();
                } else {
                    expr();
                }

        }
        if (checkSemiColon) {
            check(Token.SEMICOLON, "';' expected");
        }
    }

    private void localDec() {
        next();
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

    }

    private void repeatStat() {
        next();
        while (lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
            statement();
        }
        check(Token.UNTIL, "'until' was expected");
    }

    private void breakStat() {
        next();

    }

    private void returnStat() {
        next();
        expr();
    }

    private void whileStat() {
        next();
        expr();
        check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
        next();
        while (lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
            statement();
        }
        check(Token.RIGHTCURBRACKET, "'}' was expected");
    }

    private void ifStat() {
        next();
        expr();
        check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
        next();
        while (lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE) {
            statement();
        }
        check(Token.RIGHTCURBRACKET, "'}' was expected");
        if (lexer.token == Token.ELSE) {
            next();
            check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
            next();
            while (lexer.token != Token.RIGHTCURBRACKET) {
                statement();
            }
            check(Token.RIGHTCURBRACKET, "'}' was expected");
        }
    }

    /**

     */
    private void writeStat() {
        next();
        check(Token.DOT, "a '.' was expected after 'Out'");
        next();
        check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
        String printName = lexer.getStringValue();
        expr();
    }

    private void expr() {

    }

    private void fieldDec() {
        lexer.nextToken();
        type();
        if (lexer.token != Token.ID) {
            this.error("A variable name was expected");
        } else {
            while (lexer.token == Token.ID) {
                lexer.nextToken();
                if (lexer.token == Token.COMMA) {
                    lexer.nextToken();
                } else {
                    break;
                }
            }
        }

    }

    private void type() {
        if (lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING) {
            next();
        } else if (lexer.token == Token.ID) {
            next();
        } else {
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
    public Statement assertStat() {

        lexer.nextToken();
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
