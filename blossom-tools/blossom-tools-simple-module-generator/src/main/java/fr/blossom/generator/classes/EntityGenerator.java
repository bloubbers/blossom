package fr.blossom.generator.classes;

import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;
import fr.blossom.core.common.entity.AbstractEntity;
import fr.blossom.generator.configuration.model.Field;
import fr.blossom.generator.utils.GeneratorUtils;
import fr.blossom.generator.configuration.model.Settings;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

public class EntityGenerator implements ClassGenerator {

  @Override
  public void prepare(Settings settings, JCodeModel codeModel) {

  }

  @Override
  public JDefinedClass generate(Settings settings, JCodeModel codeModel) {
    try {
      JDefinedClass definedClass = codeModel
        ._class(GeneratorUtils.getEntityFullyQualifiedClassName(settings));
      definedClass._extends(AbstractEntity.class);

      // Entity name annotation
      JAnnotationUse entityAnnotation = definedClass.annotate(Entity.class);
      entityAnnotation.param("name", settings.getEntityName());

      // Table name annotation
      JAnnotationUse tableAnnotation = definedClass.annotate(Table.class);
      tableAnnotation.param("name", settings.getEntityNameLowerUnderscore());

      // Fields
      for (Field field : settings.getFields()) {
        addField(codeModel, definedClass, field);
      }

      return definedClass;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Can't generate PO", e);
    }
  }


  private void addField(JCodeModel codeModel, JDefinedClass definedClass, Field field) {
    // Field
    JFieldVar fieldVar = definedClass.field(JMod.PRIVATE, codeModel.ref(field.getClassName()), field.getName());
    JAnnotationUse columnAnnotation = fieldVar.annotate(Column.class);
    columnAnnotation.param("name", field.getColumnName());

    if (field.getMaxLength() != null) {
      columnAnnotation.param("length", field.getMaxLength());
    }

    if(field.isLob()){
      fieldVar.annotate(Lob.class);
    }

    if (field.getTemporalType() != null) {
      JAnnotationUse temporalTypeAnnotation = fieldVar.annotate(Temporal.class);
      temporalTypeAnnotation.param("value", codeModel.ref(TemporalType.class).staticRef(field.getTemporalType()));
    }

    // Getter
    JMethod getter = definedClass.method(JMod.PUBLIC, fieldVar.type(), field.getGetterName());
    getter.body()._return(fieldVar);

    // Setter
    JMethod setter = definedClass.method(JMod.PUBLIC, void.class, field.getSetterName());
    JVar param = setter.param(fieldVar.type(), field.getName());
    setter.body().assign(JExpr.refthis(fieldVar.name()), param);
  }
}
