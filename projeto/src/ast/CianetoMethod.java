package ast;

import java.util.Hashtable;

public class CianetoMethod {
    private String name;
    private String qualifier;
    private String type;
    private Hashtable<String, Object> parameters;
    private Hashtable<String, Object> localVariables;

    public CianetoMethod(String name, String qualifier) {
        this.name = name;
        this.qualifier = qualifier;
        this.parameters = new Hashtable<String, Object>();
        this.localVariables = new Hashtable<String, Object>();
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

    public void setType(String type) { this.type = type; }

    public String getType() { return type; }

    public CianetoAttribute getParameterById(String key) {
        return (CianetoAttribute) this.parameters.get(key);
    }

    public Hashtable<String, Object> getParameter(){ return this.parameters; }

    public void putParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public CianetoAttribute getLocal(String key) {
        return (CianetoAttribute) this.localVariables.get(key);
    }

    public void putVariable(String key, Object value) {
        this.localVariables.put(key, value);
    }
}
