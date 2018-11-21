# Laboratório de Compiladores 2018/2
Projeto da disciplina de compiladores 2018/2

# Análise Léxica
Fornecida no compilador base, aparentemente ok

# Análise Sintática
Parcialmente fornecida no compilador base

### Chamadas revisadas ou criadas
Aparentemente implementada por completo


- [x] program
- [x] annot
    - [x] annotParam
- [x] classDec
- [x] memberList
- [x] qualifier
- [x] fieldDec
    - [x] type | BasicType | Id
- [x] methodDec
    - [x] formalParamDec `TODO analise semantica`
        - [x] paramDec `TODO analise semantica`
    - [x] statementList
        - [x] statement
            - [x] localDec
            - [x] repeatStat `?`
            - [x] breakStat
            - [x] returnStat
            - [x] whileStat
            - [x] ifStat    `?`
            - [x] writeStat `?`
            - [x] assignExpr `TODO analise semantica`
            - [x] expr `TODO Cabuloso`
                - [x] relation
                - [x] simpleExpr
                - [x] sumSubExpr
                    - [x] lowOperator
                - [x] term
                    - [x] highOperator
                - [x] signalFactor
                    - [x] signal
                - [x] factor `TODO`
                    - [x] primaryExpr
                        - [x] ExprList


# Análise Semântica
Necessário implementação por completo

Para analisar se a expr é Boolean iremos ver se os tipos das variaveis são os mesmos e se possui `==` | `<` | `>` | `<=` | `>=` | `!=`

Inserindo classes na tabela hash e seus atributos

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