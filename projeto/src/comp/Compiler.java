
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import ast.*;
import jdk.nashorn.internal.codegen.types.BooleanType;
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
        symbolTable = new SymbolTable(); // Inicializando a SymbolTable
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
                } else {
                    break;
                }
            }

            // Se depois que ler o annotParam nao encontrar ')', lanca um erro
            if (lexer.token != Token.RIGHTPAR) {
                error("')' expected after metaobject call with parameters");
            } else {
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
        CianetoClass cianetoClass = new CianetoClass();
        boolean OPEN = false;
        if (lexer.token == Token.ID && lexer.getStringValue().equals("open")) {
            OPEN = true;
            next();
        }

        cianetoClass.setOpenClass(OPEN); // Setando se a classe eh open ou nao

        // Se nao encontrar a palavra 'class', lanca um erro
        if (lexer.token != Token.CLASS) {
            error("'class' expected");
        }

        next();

        // Se nao encontrar um ID depois de 'class', lanca um erro
        if (lexer.token != Token.ID) {
            error("Identifier expected");
        }

        // Recuperando o ID da classe (nome da classe)
        String className = lexer.getStringValue();
        cianetoClass.setName(className);
        next();

        // Se encontrar a palavra 'extends' apos o ID
        if (lexer.token == Token.EXTENDS) {
            next();

            // Precisa existir um outro ID (uma classe herda de outra classe), se nao encontrar lanca um erro
            if (lexer.token != Token.ID) {
                error("Identifier expected");
            }

            // Recuperando o ID da super classe
            String superclassName = lexer.getStringValue();
            CianetoClass dad = symbolTable.returnClass(superclassName);

            if (dad == null) {
                error("Did not find class '" + superclassName + "', class '" + cianetoClass.getName() + "' cannot extends a closed class or not decleared class");
            }

            // Salvando a classe pai da classe atual
            cianetoClass.setDad(dad);
            next();
        }

        // Atualizando a classe atual, ou seja, qual classe está sendo analizada no momento
        // Após isso chama o memberList
        actualClass = cianetoClass;
        memberList();

        // Se nao encontrar a palavra 'end', lanca um erro
        if (lexer.token != Token.END) {
            error("'end' expected");
        }

        // A classe terminou de ser analisada
        actualClass = null;
        symbolTable.putClass(className, cianetoClass);

        next();
    }

    // memberList ::= { [ qualifier ] member }
    //                  Se não entrar em nada no qualifier, assume que o metodo ou atributo eh publico
    private void memberList() {
        // Enquanto houver um metodo
        while (true) {
            // Verifica se o metodo eh public, private, override, etc..
            String q = qualifier();

            // Se nao encontrou nada no qualifier, pode ser uma variavel ou uma funcao ou nada
            if (lexer.token == Token.VAR) {
                fieldDec(q);
            } else if (lexer.token == Token.FUNC) {
                methodDec(q);
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
    private void methodDec(String q) {
        // TODO: Adicionar o Id encontrar na tabela hash (Semantica)
        // TODO: Verificar se existem metodos iguais
        // TODO: Verificar se existe metodo override na classe pai
        // q eh o qualifier!!
        next(); // Ja verificou se tinha 'func' na chamada anterior

        String methodName = "";
        CianetoMethod method = new CianetoMethod("", "");

        // Se for apenas ID, o metodo nao possui parametros
        if (lexer.token == Token.ID) {
            // Recuperando o nome do metodo
            methodName = lexer.getStringValue();

            // Setando o nome do metodo e seu qualificador, alem de coloca-lo como metodo atual
            method.setName(methodName);
            method.setQualifier(q);
            actualMethod = method;

            next();
        }
        // Se nao pode ser um metodo ('Identifier:') com parametros
        else if (lexer.token == Token.IDCOLON) {
            // Recuperando o nome do metodo e removendo o ';'
            methodName = lexer.getStringValue().replaceAll(";", "");

            // Setando o nome do metodo e seu qualificador, alem de coloca-lo como metodo atual
            method.setName(methodName);
            method.setQualifier(q);
            actualMethod = method;

            next();
            formalParamDec();
        } else {
            error("An identifier or identifer: was expected after 'func'");
        }

        // Se encontrar '->', o metodo retorna alguma coisa
        if (lexer.token == Token.MINUS_GT) {
            // method declared a return type
            next();
            String t = type();
            actualMethod.setType(t);
        }

        // Verifica se o token eh diferente '{'
        if (lexer.token != Token.LEFTCURBRACKET) {
            error("'{' expected");
        }

        next();
        statementList();

        // Verifica se o token eh diferente '}'
        if (lexer.token != Token.RIGHTCURBRACKET) {
            error("'}' expected");
        }

        // O metodo terminou de ser analisado e salvando o metodo na classe atual
        actualMethod = null;
        actualClass.putMethod(methodName, method);

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
        String type = type();

        // Se nao encontrar um identificador apos o tipo de uma variavel, lanca um erro
        if (lexer.token != Token.ID) {
            error("A variable name was expected");
        }

        // Recuperando o nome da variavel, criando o objeto que será salvo no parametro do metdodo atual
        String variableName = lexer.getStringValue();
        CianetoAttribute variable = new CianetoAttribute(variableName, type, "");
        actualMethod.putParameter(variableName, variable);

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
                 checkSemiColon = false;
                break;
            case REPEAT:
                repeatStat();
                break;
            case VAR:
                localDec();
                checkSemiColon = false; // ';' eh opcional
                break;
            case ASSERT:
                assertStat();
                break;
            default:

                // Se for um Id e o Id for igual a 'Out', chama o writeStat
                if (lexer.token == Token.ID && lexer.getStringValue().equals("Out")) {
                    checkSemiColon = true;
                    writeStat();
                }
                // Se nao chama o Assign
                else if (lexer.token == Token.ID) {
                    assignExpr();
                } else {
                    checkSemiColon = false;
                    next();
                }
        }

        // Se nao for ifStat nem whileStat, nao verifica se tem ';'
        if (checkSemiColon) {
            check(Token.SEMICOLON, "';' expected");
            next();
        }
    }

    // localDec ::= 'var' Type IdList [ '=' Expression ] [';']
    private void localDec() {
        int countIdList = 0;
        next(); // le token 'var'
        String t = type();
        check(Token.ID, "A variable name was expected");
        while (lexer.token == Token.ID) {
            countIdList++; // contar numero de variaveis sendo declaradas
            String id = lexer.getStringValue();
            CianetoAttribute c = new CianetoAttribute(id, t, null);
            if (actualMethod.getLocal(id) == null){ // verifica se variavel local já existe
                actualMethod.putVariable(id, c);
            }else{
                error("variable already declared");
            }
            next();
            if (lexer.token == Token.COMMA) {
                next();
            } else {
                break;
            }
        }

        if (lexer.token == Token.ASSIGN) {
            if (countIdList > 1){ // verificar se existe mais de uma variavel sendo atribuida
                error("More than one variable assigned");
            }
            next();
            // check if there is just one variable
            Expr e = expr();
            if (!e.getType().equals(t)){ // verificar se os tipos são compativeis
                error("return of expression not compatible");
            }
        }

        // ';' eh opcional
        if (lexer.token == Token.SEMICOLON) {
            next(); // Como ';' eh final, anda o token
        }

    }

    // repeatStat ::= 'repeat' StatementList 'until' Expression
    private void repeatStat() {
        next(); // le token 'repeat'

        // Anotações do professor
//        while (lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END) {
//            statement();
//        }

        statementList();

        check(Token.UNTIL, "'until' was expected");
        next(); // le token 'until'

        Expr e = expr();
        if (!e.getType().equals("boolean")){ // verificar se a expressao e boolean
            error("expression must be boolean");
        }

    }

    // breakStat ::= 'break'
    private void breakStat() {
        next(); // le o token 'break'
    }

    // returnStat ::= 'return' Expression
    private void returnStat() {
        next(); // le o token 'return'
        Expr e = expr();
        if (!e.getType().equals(actualMethod.getType())){ // verifica se o retorno do metodo e do mesmo tipo da expressao
            error("expression not compatible to method return");
        }
    }

    // whileStat ::= 'while' Expression '{' StatementList '}'
    private void whileStat() {
        next(); // le o token 'while'
        Expr e = expr();

        if (!e.getType().equals("boolean")){ // verifica se expressao e boolean
            error("expression must be boolean");
        }

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
        next(); // le o token 'if'
        Expr e = expr();

        if (!e.getType().equals("boolean")){ // verifica se expressao e boolean
            error("expression must be boolean");
        }

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
        if (!printName.equals("print:") && !printName.equals("println:")) {
            this.error("'print:' or 'println:' was expected after 'Out.'");
        }

        next();
        expr();
    }

    // assignExpr ::= Expression [ '=' Expression ]
    private void assignExpr() {
        Expr first = expr();

        // Se encontrar um '='
        if (lexer.token == Token.ASSIGN) {
            next(); // Le o token '='
            Expr second = expr();

            if (!first.getType().equals(second.getType())){ // verifica se atribuição é do mesmo tipo
                error("Incompatible types");
            }
        }
    }

    // expr ::= SimpleExpression [ Relation SimpleExpression ]
    private Expr expr() {
        // TODO: Comparação entre classes com a definicão de convertible do pdf
        Expr first = simpleExpr();

        // Se for uma expressao de relacao '==' | '<' | '>' | '<=' ...
        if (relation()) {
            String relation = lexer.token.toString();
            next();
            Expr second = simpleExpr();

            if (relation.equals("<") || relation.equals("<=") || relation.equals(">=") || relation.equals(">")) {
                if (!first.getType().equals("int") && !second.getType().equals("int")){
                    error("expressions must be int");
                }
            }else if (relation.equals("==") || relation.equals("!=")){
                if (first.getType().equals("string") && (!second.getType().equals("null") || !second.getType().equals("string"))){
                    error("string must compare with string or null");
                }else if (second.getType().equals("string") && (!first.getType().equals("null") || !first.getType().equals("string"))){
                    error("string must compare with string or null");
                }else if (!first.getType().equals(second.getType())){
                    error("incompatible types");
                }
            }

        }

        return first; // somente para não dar erro
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
    private Expr simpleExpr() {
        boolean flag = false;
        Expr first = sumSubExpr();

        // Se encontrou '+'
        while (lexer.token == Token.PLUS) {
            flag = true;
            next(); // Consome o primeiro '+'
            if (lexer.token == Token.PLUS){
                next(); // Consome o segundo
            } else {
                error("'+' expected");
            }

            Expr n = sumSubExpr();

            if (!n.getType().equals("int") && !n.getType().equals("string")){
                error("incompatible types to concat");
            }
        }

        if (flag) {
            if (!first.getType().equals("int") && !first.getType().equals("string")) {
                error("incompatible types to concat");
            }

            return new Expr("string");
        }

        return first;
    }

    // sumSubExpr ::= Term { LowOperator Term }
    private Expr sumSubExpr() {
        boolean intValues = false;
        boolean booleanValues = false;
        Expr first = term();

        // Enquanto encontrar '+' | '-' | '||' continua no while
        while (lowOperator()) {
            String low = lexer.token.toString();
            next(); // Consome o simbolo

            Expr n = term();

            if (low.equals("+") || low.equals("-")) {
                intValues = true;
                if (!n.getType().equals("int") || booleanValues){
                    error("incompatible types in low operator");
                }
            } else if (low.equals("||")) {
                booleanValues = true;
                if (!n.getType().equals("boolean") || intValues) {
                    error("incompatible types in low operator");
                }
            }
        }

        if (intValues) {
            if (!first.getType().equals("int")) {
                error("incompatible types in low operator");
            }
        }

        if (booleanValues) {
            if (!first.getType().equals("boolean")) {
                error("incompatible types in low operator");
            }
        }

        return first;
    }

    private boolean lowOperator() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
            return true;
        }

        return false;
    }

    // term ::= SignalFactor { HighOperator SignalFactor }
    private Expr term() {
        Expr first = signalFactor();

        // Enquanto encontrar '*' | '/' | '&&' continua no while
        while (highOperator()) {
            next(); // Consome o simbolo
            Expr second = signalFactor();

            if (!first.getType().equals(second.getType())){ // verifica se atribuição é do mesmo tipo
                error("incompatible types");
            }
        }

        return first;
    }

    private boolean highOperator() {
        if (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
            return true;
        }

        return false;
    }

    // signalFactor ::= [ Signal ] Factor
    private Expr signalFactor() {
        // Pode encontrar '+' | '-', se encontrar
        if (signal()) {
            next(); // Consome o simbolo
        }

         return factor();
    }

    private boolean signal() {
        if (lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
            return true;
        }

        return false;
    }

    // factor ::= BasicValue | '(' Expression ')' | '!' Factor | 'nil' | ObjectCreation | PrimaryExpr
    private Expr factor() {
        // TODO: BasicValue eh variavel do tipo INT ou INTLITERAL, STRING OU STRINGLITERAL  ?
        Expr e = null;
        switch (lexer.token) {
            case LITERALINT:
                intValue();
                e = new Expr("int");
                break;
            case LITERALSTRING:
                stringValue();
                e = new Expr("string");
                break;
            case TRUE:
                booleanValue();
                e = new Expr("boolean");
                break;
            case FALSE:
                booleanValue();
                e = new Expr("boolean");
                break;
            case LEFTPAR:
                next(); // Consome o '('
                e = expr();

                check(Token.RIGHTPAR, "')' expected");
                next(); // Consome o ')'
                break;
            case NOT:
                e = factor();
                break;
            case NULL:
                next(); // Le o 'nil'

                break;
            default:

                // TODO: Verificar como identificar qual metodo chamar objectCreation | primaryExpr
                // verifica objectCreation e Id. do primaryExpr
                if (lexer.token == Token.ID) {
                    next();
                    if (lexer.token == Token.DOT){
                        next();
                        if (lexer.token == Token.NEW){
                            next(); // consumindo o objectCreation
                            // TODO: Armazenar o objeto criado
                        }else {
                            if (lexer.token == Token.ID){
                                next(); // consomindo Id
                            }else if (lexer.token == Token.IDCOLON){
                                next();
                                exprList();
                            }else {
                                error("Id or IdColon was expected");
                            }
                        }
                    }
                } else {
                    e = primaryExpr();
                }
        }

        return e;
    }

    /** basicValue ::= IntValue | BooleanValue | StringValue **/
    // IntValue ::= Digit { Digit }
    private void intValue() {
        next();
    }

    // StringValue ::= " Letter { Letter } "
    private void stringValue() {
        next();
    }

    // BooleanValue ::= true || false
    private void booleanValue() {
        next();
    }

    // primaryExpr ::=  'super' '.' IdColon ExpressionList | 'super' '.' Id |     PODE COMECAR COM SUPER
    //                  Id | Id '.' Id | Id '.' IdColon ExpressionList |          PODE COMECAR COM ID
    //                  'self' | 'self' '.' Id | 'self' '.' IdColon ExpressionList |          PODE COMECAR COM SELF
    //                  'self' '.' Id '.' IdColon ExpressionList | 'self' '.' Id '.' Id |     PODE COMECAR COM SELF
    //                  ReadExpr        OU PODE SER READEXPR
    private Expr primaryExpr() {
//        Expr expression = null;
        String auxType = "";
        switch (lexer.token){
            case SUPER:
                // TODO: Ver se é possivel acessar as variaveis globais da classe pai
                next(); // consome "super"
                check(Token.DOT, "a '.' was expected after 'super'");
                next(); // consome "."

                if (lexer.token == Token.ID){
                    // TODO: Verificar também se eh um attr
                    /*
                     * class FOO
                     *      var Int a;  // OK
                     *      var Int b   // OK
                     * end
                     */
                    String method = lexer.getStringValue();
                    if (actualClass.getDad().getMethod(method) == null){ // verifica se a classe pai tem o metodo
                        error("Super class haven't this method");
                    }else{
                        if(actualClass.getDad().getMethod(method).getQualifier().equals("private")){
                            error("cannot access this method");
                            // TODO: verificar a classe pai da pai se tem o attr ou metodo chamado
                        }
                    }

                    // Recuperando o tipo do método chamado da classe pai
                    auxType = actualClass.getDad().getMethod(method).getType();
                    next(); // consome o ID
                } else if (lexer.token == Token.IDCOLON){
                    String method = lexer.getStringValue();
                    if (actualClass.getDad().getMethod(method) == null){ // verifica se a classe pai tem o metodo
                        error("Super class haven't this method");
                    }else{
                        if(actualClass.getDad().getMethod(method).getQualifier().equals("private")){
                            error("cannot access this method");
                        }
                    }

                    auxType = actualClass.getDad().getMethod(method).getType();

                    next(); // consome o ID colon

                    List<Expr> e = exprList(); // lista de paramentros
                    Hashtable<String, Object> params = actualClass.getDad().getMethod(method).getParameter(); // lista de paramentros salva no metodo

                    if (e.size() != params.size()) { // verifica se tem a mesma quantidade de parametros
                        error("expected a different number of parameters");
                    }

                    Set<String> keyParams = params.keySet();
                    int i = 0;
                    for (String key: keyParams){ // verifica se tem o mesmo tipo
                        CianetoAttribute c = (CianetoAttribute) params.get(key);
                        if (!c.getType().equals(e.get(i).getType())){
                            error("incompatible types in parameters");
                        }
                        i++;
                    }
                } else {
                    error("an Id or IdColon was expected");
                }
//                expression = new Expr(auxType);
                break;
            case SELF:
                next(); // consome "self"

                // Pode ter ou nao '.'
                if (lexer.token == Token.DOT) {
                    next(); // Consome o '.'

                    if (lexer.token == Token.ID){
                        String value = lexer.getStringValue();
                        // verifica se existe algum metodo ou atributo nessa classe
                        if (actualClass.getAttribute(value) == null && actualClass.getMethod(value) == null){
                            error("method or attribute not found");
                        }

                        // Recuperando o tipo do metodo ou attr
                        if (actualClass.getAttribute(value) == null) {
                            auxType = actualClass.getMethod(value).getType();
                        } else {
                            auxType = actualClass.getAttribute(value).getType();
                        }

                        if (lexer.token == Token.DOT){
                            next(); // consome '.'
                            if (lexer.token == Token.ID){

                                String s = lexer.getStringValue();

                                if (actualClass.getAttribute(value) != null){ // verifica se existe um metodo ou atributo na classe
                                    CianetoClass currentClass = symbolTable.returnClass(actualClass.getAttribute(value).getType());
                                    if (currentClass.getMethod(s) == null && currentClass.getAttribute(s) == null){
                                        boolean exists = false;
                                        CianetoClass dad = symbolTable.returnClass(actualClass.getAttribute(value).getType()).getDad();
                                        while (dad != null){
                                            if (dad.getMethod(s) != null || dad.getAttribute(s) != null){
                                                exists = true;
                                                break;
                                            }
                                            dad = dad.getDad();
                                        }
                                        if (!exists){
                                            error("method or attribute not found");
                                        } else {
                                            // Recuperando o tipo dometodo ou attr da classe pai
                                            if (dad.getMethod(s) == null) {
                                                auxType = dad.getAttribute(s).getType();
                                            } else {
                                                auxType = dad.getMethod(s).getType();
                                            }
                                        }
                                    }
                                }else if (actualClass.getMethod(value) != null){ // verifica se existe um medoto ou atributo na classe do retorno do metodo
                                    CianetoClass currentClass = symbolTable.returnClass(actualClass.getMethod(value).getType());
                                    if (currentClass.getMethod(s) == null && currentClass.getAttribute(s) == null){
                                        boolean exists = false;
                                        CianetoClass dad = symbolTable.returnClass(actualClass.getAttribute(value).getType()).getDad();
                                        while (dad != null){
                                            if (dad.getMethod(s) != null || dad.getAttribute(s) != null){
                                                exists = true;
                                                break;
                                            }
                                            dad = dad.getDad();
                                        }
                                        if (!exists){
                                            error("method or attribute not found");
                                        }
                                        else {
                                            // Recuperando o tipo dometodo ou attr da classe pai
                                            if (dad.getMethod(s) == null) {
                                                auxType = dad.getAttribute(s).getType();
                                            } else {
                                                auxType = dad.getMethod(s).getType();
                                            }
                                        }
                                    }
                                }

                                next(); // consome o ID
                            } else if (lexer.token == Token.IDCOLON){
                                String method = lexer.getStringValue();
                                CianetoMethod current = null;

                                if (actualClass.getAttribute(value) != null){ // verifica se existe um metodo ou atributo na classe
                                    if (symbolTable.returnClass(actualClass.getAttribute(value).getType()).getMethod(method) == null){
                                        error("method not found");
                                    }
                                    current = symbolTable.returnClass(actualClass.getAttribute(value).getType()).getMethod(method);
                                }else if (actualClass.getMethod(value) != null){ // verifica se existe um medoto ou atributo na classe do retorno do metodo
                                    if (symbolTable.returnClass(actualClass.getMethod(value).getType()).getMethod(method) == null){
                                        error("method not found");
                                    }
                                    current = symbolTable.returnClass(actualClass.getMethod(value).getType()).getMethod(method);
                                }

                                next(); // consome o ID colon

                                List<Expr> e = exprList(); // lista de paramentros
                                Hashtable<String, Object> params = current.getParameter(); // lista de paramentros salva no metodo

                                if (e.size() != params.size()) { // verifica se tem a mesma quantidade de parametros
                                    error("expected a different number of parameters");
                                }

                                Set<String> keyParams = params.keySet();
                                int i = 0;
                                for (String key: keyParams){ // verifica se tem o mesmo tipo
                                    CianetoAttribute c = (CianetoAttribute) params.get(key);
                                    if (c.getType() != e.get(i).getType()){
                                        error("incompatible types in parameters");
                                    }
                                }

                                // Recuperando o tipo a lista de expr
                                auxType = e.get(0).getType();
                            } else {
                                error("an Id or IdColon was expected");
                            }
                        }

                        next(); // consome o ID
                    } else if (lexer.token == Token.IDCOLON) {
                        String method = lexer.getStringValue();
                        next(); // consome o ID colon

                        List<Expr> e = exprList(); // lista de paramentros
                        Hashtable<String, Object> params = actualClass.getMethod(method).getParameter(); // lista de paramentros salva no metodo

                        if (e.size() != params.size()){ // verifica se tem a mesma quantidade de parametros
                            error("expected a different number of parameters");
                        }

                        Set<String> keyParams = params.keySet();
                        int i = 0;
                        for (String key: keyParams){ // verifica se tem o mesmo tipo
                            CianetoAttribute c = (CianetoAttribute) params.get(key);
                            if (c.getType() != e.get(i).getType()){
                                error("incompatible types in parameters");
                            }
                        }

                        // Recuperando o tipo a lista de expr
                        auxType = e.get(0).getType();
                    }else {
                        error("an Id or IdColon was expected");
                    }
                }
                break;
            case IN: // Aparentemente está certo, a analise sintatica verifica se eh 'int' ou 'string'
                readExpr();
                break;
            case ID: // TODO
                next(); // consome o 'ID'

                // Pode ter ou nao '.'
                if (lexer.token == Token.DOT) {
                    next(); // consome o '.'

                    if (lexer.token == Token.ID) {
                        next(); // Consome o 'ID'
                    } else if (lexer.token == Token.IDCOLON) {
                        next(); // Consome o 'IDCOLON'
                        exprList();
                    } else {
                        error("An Id or IdColon was expected after '.'");
                    }
                }
                break;
            default:
                error("expected 'In', 'super', 'self' or 'id'");
        }

        return new Expr(auxType);
    }

    // exprList ::= Expression { ',' Expression }
    private List<Expr> exprList() {
        // TODO: Verificar o tipo no exprList
        Expr first = expr();

        // Enquanto encontrar ',' chama o expr()
        while (lexer.token == Token.COMMA){
            next(); // Consome o ','
            Expr second = expr();
        }

        return new ArrayList<>(); // só pra nao dar erro
    }

    // readExpr ::= 'In' '.' [ 'readInt' | 'readString' ]
    private void readExpr() {
        next(); // consome o 'In', verificou na chamada anterior
        check(Token.DOT, "a '.' was expected after 'In'");
        next(); // consome o '.'
        if (lexer.token == Token.READINT || lexer.token == Token.READSTRING){
            next();
        } else {
            error("expected 'readInt' or 'readString' after '.'");
        }
    }

    // fieldDec ::= 'var' Type IdList ';'
    private void fieldDec(String q) {
        // TODO: Adicionar o Id encontrar na tabela hash (Semantica)
        next(); // Ja verificou se tinha 'var' na chamada anterior
        String t = type(); // Verificado

        // Se nao encontrar um Id depois do tipo, lanca um erro
        if (lexer.token != Token.ID) {
            this.error("A variable name was expected");
        } else {
            // Enquanto existirem mais Id
            while (lexer.token == Token.ID) {
                String id = lexer.getStringValue();

                CianetoAttribute a = new CianetoAttribute(id, t, q); // id, type, qualifier
                actualClass.putAttribute(id, a);
                next(); // Anda o token

                // Ve se encontrou uma virgula
                if (lexer.token == Token.COMMA) {
                    next(); // Anda o token

                    // O ';' eh opcional, portanto se encontra-lo ou nao para o laco
                } else if (lexer.token == Token.SEMICOLON) {
                    next(); // Como ';' eh final, anda o token
                    break;
                } else {
                    break;
                }
            }
        }

    }

    // type ::= BasicType | Id
    private String type() {
        // BasicType ::= 'Int' | 'Boolean' | 'String'
        if (lexer.token == Token.INT || lexer.token == Token.BOOLEAN || lexer.token == Token.STRING) {
            String idType = lexer.getStringValue();
            next();
            return idType;
        } else if (lexer.token == Token.ID) {
            String idType = lexer.getStringValue();
            // TODO: Verifica se o id existe (classe)
            next();
            return idType;
        }
        // Lança um erro se não for ID nem um tipo basico
        else {
            this.error("A type was expected");
        }

        return "";
    }

    // qualifier ::=    'private' | 'public' | 'override' | 'override' 'public' |
    //                  'final' | 'final' 'public' | 'final' 'override' |
    //                  'final' 'override' 'public'
    private String qualifier() {
        String q = "";
        // Parte ja implementada pelo professor
        if (lexer.token == Token.PRIVATE) { // private
            next(); q = "private";
        } else if (lexer.token == Token.PUBLIC) { // public
            next(); q = "public";
        } else if (lexer.token == Token.OVERRIDE) { // override
            next(); q = "override";
            if (lexer.token == Token.PUBLIC) { // override public
                next(); q = "override public";
            }
        } else if (lexer.token == Token.FINAL) { // final
            next(); q = "final";
            if (lexer.token == Token.PUBLIC) { // final public
                next(); q = "final public";
            } else if (lexer.token == Token.OVERRIDE) { // final override
                next(); q = "final override";
                if (lexer.token == Token.PUBLIC) { // final override public
                    next(); return "final override public";
                }
            }
        }
        return q;
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
        // CianetoMethod intValue returns that value as an value of type int.
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
    private CianetoClass actualClass;
    private CianetoMethod actualMethod;

}
