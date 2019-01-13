package ast;

public class Expr {

    private String type;
    public Expr(final String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public void setType(final String type){
        this.type = type;
    }
}