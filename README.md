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

# Testes
- [ ] `arquivo` - `motivo do erro caso nao passe no teste`
- [ ] `ER-LEX-EXTRA01.ci` - `Erro encontrado na linha 14, deveria ser na 29`
- [ ] `er-lex01.ci` - `nao tem anotação no teste, não é testado`
- [ ] `er-lex02.ci` - `nao tem anotação no teste, não é testado`
- [x] `ER-LEX03.ci`
- [x] `ER-LEX04.ci` - `Erro sinalizado corretamente, mas pritando pilha de erros do java no terminal`
- [x] `ER-LEX05.ci`
- [x] `ER-LEX06.ci`
- [x] `ER-LEX07.ci`
- [x] `ER-LEX08.ci`
- [ ] `ER-SEM-100.ci` - `Anotação do erro depois da declaração da primeira classe, não é testado`
- [x] `ER-SEM-EXTRA01.ci`
- [ ] `ER-SEM-EXTRA02.ci` - `Erro encontrado na linha 14, deveria ser na 36`
- [x] `ER-SEM01.ci`
- [x] `ER-SEM02.ci`
- [x] `ER-SEM03.ci`
- [x] `ER-SEM04.ci`
- [x] `ER-SEM05.ci`
- [x] `ER-SEM07.ci` - `Melhorar mensagem de erro?`
- [x] `ER-SEM08.ci` - `Melhorar mensagem de erro?`
- [x] `ER-SEM09.ci` - `Melhorar mensagem de erro?`
- [x] `ER-SEM11.ci`
- [x] `ER-SEM12.ci` - `Melhorar mensagem de erro?`
- [ ] `ER-SEM13.ci` - `Pilha de erros do java, não é testado` arquivo não é aberto p/ leitura provavelmente
- [x] `ER-SEM14.ci`
- [x] `ER-SEM15.ci`
- [ ] `ER-SEM16.ci` - `Pilha de erros do java, não é testado` arquivo não é aberto p/ leitura provavelmente
- [ ] `ER-SEM17.ci` - `Pilha de erros do java, não é testado` arquivo não é aberto p/ leitura provavelmente
- [x] `ER-SEM18.ci`
- [x] `ER-SEM19.ci`
- [x] `ER-SEM20.ci`
- [x] `ER-SEM21.ci`
- [ ] `ER-SEM22.ci` - `Pilha de erros do java, não é testado` arquivo não é aberto p/ leitura provavelmente

# Testes anteriores
- [x] `OK-LEX02`
- [x] `OK-LEX03`
- [x] `OK-LEX04`
- [x] `OK-LEX05`
- [x] `OK-LEX06`
- [x] `OK-LEX08`
- [x] `OK-LEX10`
- [ ] `ok-math` - `Erro no Comp`
- [ ] `ok-queue` - `Erro no Comp`
- [x] `OK-SEM03`
- [x] `OK-SEM04`