package ast;

public class CianetoAttribute {
    private String name;
    private String type;
    private String qualifier;

    public CianetoAttribute() { }

    public CianetoAttribute(String name, String type, String qualifier) {
        this.name = name;
        this.type = type;
        this.qualifier = qualifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }
}
