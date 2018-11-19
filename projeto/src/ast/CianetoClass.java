package ast;
/*
 * Krakatoa Class
 */
public class CianetoClass extends Type {

   public CianetoClass( String name ) {
      super(name);
   }

   @Override
   public String getCname() {
      return getName();
   }

   private String name;
   private CianetoClass superclass;
   // private FieldList fieldList;
   // private MethodList publicMethodList, privateMethodList;
   // metodos publicos get e set para obter e iniciar as variaveis acima,
   // entre outros metodos
}
