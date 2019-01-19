# Laboratório de Compiladores 2018/2
Projeto da disciplina de compiladores 2018/2

# Análise Léxica
Fornecida no compilador base, aparentemente ok

# Análise Sintática
Parcialmente fornecida no compilador base

### Chamadas revisadas ou criadas
Aparentemente implementada por completo, fazer testes e ver se a lógica está correta!!


- [x] program
- [x] annot
    - [x] annotParam
- [x] classDec
- [x] memberList
- [x] qualifier
- [x] fieldDec
    - [x] type | BasicType | Id
- [x] methodDec
    - [x] formalParamDec
        - [x] paramDec
    - [x] statementList
        - [x] statement
            - [x] localDec
            - [x] repeatStat
            - [x] breakStat
            - [x] returnStat
            - [x] whileStat
            - [x] ifStat   
            - [x] writeStat
            - [x] assignExpr
            - [x] expr
                - [x] relation
                - [x] simpleExpr
                - [x] sumSubExpr
                    - [x] lowOperator
                - [x] term
                    - [x] highOperator
                - [x] signalFactor
                    - [x] signal
                - [x] factor
                    - [x] primaryExpr
                        - [x] ExprList


# Análise Semântica
Necessário implementação por completo

Para analisar se a expr é Boolean iremos ver se os tipos das variaveis são os mesmos e se possui `==` | `<` | `>` | `<=` | `>=` | `!=`

Inserindo classes na tabela hash e seus atributos

- [x] classDec `Inserindo classe na symbolTable`
- [x] memberList
- [x] qualifier `Retorna o qualificador do metodo ou atributo`
- [x] fieldDec `Salva na classe atual o atributo`
- [x] methodDec `Salva na classe atual o metodo`
    - [x] formalParamDec `Chama paramDec`
        - [x] paramDec `Salvando no metodo atual a lista de parametros`
    - [x] statementList
        - [ ] statement `TODO!` 
            - [x] localDec `Salvando atributos no metodo atual e verificações`
            - [x] repeatStat `Verifica se expressao é boolean`
            - [x] breakStat
            - [x] returnStat `Verifica se retorno é do mesmo tipo que o retorno do metodo`
            - [x] whileStat `Verifica se expressao é boolean`
            - [x] ifStat `Verifica se expressao é boolean`
            - [x] writeStat `Aparentemente não precisa de verificação (?)`
            - [x] assignExpr `Verifica se as expressoes são do mesmo tipo`
            - [ ] expr
                - [ ] relation
                - [ ] simpleExpr
                - [ ] sumSubExpr
                    - [ ] lowOperator
                - [ ] term
                    - [ ] highOperator
                - [ ] signalFactor
                    - [ ] signal
                - [ ] factor
                    - [ ] primaryExpr `Feito os tratamentos do self e super`
                        - [ ] ExprList `Retornará uma Lista de Expr para verificar se os parametros estao certos`

# Updates
Classes em Cianeto são definidas como no exemplo abaixo

```
class Foo
    var Int bar;
end
```

Diferente do pdf, onde uma classe é definida com uso de `{` `}`


Atributos em Cianeto podem terminar ou não com `;`

```
class Foo
    var Int bar; // Ok
    var Int foo  // Ok
end
```

# Testes Lexicos 
OK-LEX02 - Success
OK-LEX03 - Success
OK-LEX04 - gramatica errada (?)
OK-LEX05 - Number out of limits
OK-LEX06 - { } (?)
OK-LEX08 - Success
OK-LEX10 - Success
ok-math - Erro no Comp
ok-queue - Erro no Comp
OK-SEM03 - Success
OK-SEM04 - Success


ER-LEX03 -> ER-LEX08 - OK
ER-LEX-EXTRA01 - OK
ER-SEM01 - Não faço ideia como verificar isso
ER-SEM02 -> ER-SEM07 - OK
ER-SEM08 -> ER-SEM09 - Erro no teste (OK) 


