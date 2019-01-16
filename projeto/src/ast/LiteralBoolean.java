package ast;

public class LiteralBoolean{

    public LiteralBoolean( boolean value ) {
        this.value = value;
    }

	public void genC( PW pw, boolean putParenthesis ) {
       pw.print( value ? "1" : "0" );
    }

	public Type getType() {
        return Type.booleanType;
    }

    public static LiteralBoolean True  = new LiteralBoolean(true);
    public static LiteralBoolean False = new LiteralBoolean(false);

    private boolean value;
}
