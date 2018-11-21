package ast;

import java.util.ArrayList;
import java.util.Hashtable;

public class CianetoClass {
   private String name;
   private CianetoClass dad;
   private Hashtable<String, Object> cianetoMethods;
   private Hashtable<String, Object> cianetoAttributes;
   private boolean OpenClass; // true = OPEN, false = NOT OPEN


   public CianetoClass() {
      this.cianetoMethods = new Hashtable<String, Object>();
      this.cianetoAttributes = new Hashtable<String, Object>();
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setOpenClass(boolean openClass) {
      this.OpenClass = openClass;
   }

   public boolean getOpenClass() {
      return this.OpenClass;
   }

   public CianetoClass getDad() {
      return dad;
   }

   public void setDad(CianetoClass dad) {
      this.dad = dad;
   }

   public void putAttribute(String key, Object value) {
      this.cianetoAttributes.put(key, value);
   }

   public CianetoAttribute getAttribute(String key) {
      return (CianetoAttribute) this.cianetoAttributes.get(key);
   }

   public void putMethod(String key, Object value) {
      this.cianetoMethods.put(key, value);
   }

   public CianetoMethod getMethod(String key) {
      return (CianetoMethod) this.cianetoMethods.get(key);
   }
   // private FieldList fieldList;
   // private MethodList publicMethodList, privateMethodList;
   // metodos publicos get e set para obter e iniciar as variaveis acima,
   // entre outros metodos
}
