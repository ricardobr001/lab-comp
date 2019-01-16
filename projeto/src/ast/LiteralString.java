package ast;

public class LiteralString{
    
    public LiteralString( String literalString ) { 
        this.literalString = literalString;
    }
    
    public void genC( PW pw, boolean putParenthesis ) {
        pw.print(literalString);
    }

    
    public Type getType() {
        return Type.stringType;
    }
    
    private String literalString;
}
