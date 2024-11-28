package CS4337.Project;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PascalCaseNamingStrategy implements PhysicalNamingStrategy {
  // thiis uses snake for everything so this convertis it to pascal as that is the convention for
  // this proj
  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
    return new Identifier(toPascalCase(name.getText()), name.isQuoted());
  }

  @Override
  public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
    return name;
  }

  @Override
  public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
    return name;
  }

  @Override
  public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
    return name;
  }

  @Override
  public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
    return name;
  }

  private String toPascalCase(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }

    StringBuilder pascalCaseName = new StringBuilder();
    String[] parts = name.split("_");

    for (String part : parts) {
      if (!part.isEmpty()) {
        pascalCaseName.append(part.substring(0, 1).toUpperCase());
        pascalCaseName.append(part.substring(1).toLowerCase());
      }
    }
    return pascalCaseName.toString();
  }
}
