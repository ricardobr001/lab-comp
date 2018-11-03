# Laboratório de Compiladores 2018/2
Projeto da disciplina de compiladores 2018/2

# Análise Léxica
Fornecida no compilador base, aparentemente ok

# Análise Sintática
Parcialmente fornecida no compilador base

### Chamadas revisadas
- [x] program
- [x] annot
    - [x] annotParam
- [x] classDec
- [x] memberList
- [x] qualifier
- [x] fieldDec
    - [x] type | BasicType | Id
- [x] methodDec
    - [ ] formalParamDec `TODO`
        - [ ] paramDec `TODO`
    - [x] statementList
        - [ ] statement
            - [x] localDec
            - [x] repeatStat `?`
            - [x] breakStat
            - [x] returnStat
            - [x] whileStat
            - [x] ifStat    `?`
            - [x] writeStat `?`
            - [ ] assignExpr `TODO`
            - [ ] expr `TODO Cabuloso`



### Chamadas feitas
Nenhuma chamada criada por enquanto


# Análise Semântica
Necessário implementação por completo

Para analisar se a expr é Boolean iremos ver se os tipos das variaveis são os mesmos e se possui `==` | `<` | `>` | `<=` | `>=` | `!=`

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