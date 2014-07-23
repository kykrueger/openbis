package ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DtoGenerator
{
    private static final String PACKAGE_PREFIX = "ch.ethz.sis.openbis.generic.shared.api.v3.dto";

    private int indent = 0;

    List<DTOField> fields;

    private String subPackage;

    private String className;

    private Set<String> additionalImports;

    private PrintStream outputStream = System.out;

    private Class<?> fetchOptionsClass;

    private String toStringContent;

    public DtoGenerator(String subPackage, String className, Class<?> fetchOptionsClass)
    {
        this.subPackage = subPackage;
        this.className = className;
        this.additionalImports = new HashSet<String>();
        this.fields = new LinkedList<DtoGenerator.DTOField>();

        addClassForImport(JsonProperty.class);
        addClassForImport(JsonIgnore.class);
        addClassForImport(JsonObject.class);
        addClassForImport(Serializable.class);
        addClassForImport(NotFetchedException.class);

        this.fetchOptionsClass = fetchOptionsClass;
        addSimpleField(fetchOptionsClass, "fetchOptions");
    }

    @Override
    public String toString()
    {
        return className;
    }

    private static class DTOField
    {
        String fieldName;

        /**
         * Only if different than fieldName
         */
        String persistentFieldName;

        String description;

        String definitionClassName;

        String importClassName;

        Class<?> fetchOptions;

        public DTOField(String fieldName, Class<?> fieldClass, String description, Class<?> fetchOptions)
        {
            this.fieldName = fieldName;
            this.definitionClassName = fieldClass.getSimpleName();
            if (false == (fieldClass.isArray() || fieldClass.isPrimitive()))
            {
                this.importClassName = fieldClass.getCanonicalName();
            }
            this.description = description;
            this.fetchOptions = fetchOptions;
        }

        public DTOField(String fieldName, String className, String fullClassName, String description, Class<?> fetchOptions)
        {
            this.fieldName = fieldName;
            this.definitionClassName = className;
            this.importClassName = fullClassName;
            this.description = description;
            this.fetchOptions = fetchOptions;
        }

        String getPersistentName()
        {
            return persistentFieldName == null ? fieldName : persistentFieldName;
        }

        String getCapitalizedName()
        {
            return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }

    }

    public void setOutputStream(PrintStream printStream)
    {
        this.outputStream = printStream;
    }

    public void addSimpleField(Class<?> c, String name)
    {
        fields.add(new DTOField(name, c, null, null));
    }

    /**
     * This method is intended for cases when field name is illegal identifier in java (e.g. keyword)
     */
    public void addSimpleField(Class<?> c, String name, String persistentFieldName)
    {
        DTOField dtoField = new DTOField(name, c, null, null);
        dtoField.persistentFieldName = persistentFieldName;
        fields.add(dtoField);
    }

    public void addFetchedField(Class<?> c, String name, String description, Class<?> fetchOptionsClass)
    {
        fields.add(new DTOField(name, c, description, fetchOptionsClass));
    }

    public void addFetchedField(String definitionClassName, String importClassName, String name, String description, Class<?> fetchOptionsClass)
    {
        fields.add(new DTOField(name, definitionClassName, importClassName, description, fetchOptionsClass));
    }

    public void addClassForImport(Class<?> c)
    {
        additionalImports.add(c.getName());
    }

    public void setToStringMethod(String toStringContent)
    {
        this.toStringContent = toStringContent;
    }

    private void print(String code, Object... formatArguments)
    {
        if (indent > 0 && code.length() > 0)
        {
            outputStream.print(String.format("%" + indent + "s", ""));
        }
        outputStream.println(String.format(code, formatArguments));
    }

    private void startBlock()
    {
        print("{");
        indent += 4;
    }

    private void endBlock()
    {
        indent -= 4;
        print("}");
    }

    public void generateDTO() throws FileNotFoundException
    {
        generateDTO("source/java/ch/ethz/sis/openbis/generic/shared/api/v3/dto/entity/" + subPackage + "/" + className + ".java");
    }

    private void generateDTO(String file) throws FileNotFoundException
    {
        PrintStream fos = new PrintStream(new FileOutputStream(file, false));
        try
        {
            generateDTO(fos);
        } finally
        {
            fos.close();
        }
    }

    public void generateDTO(PrintStream os)
    {
        setOutputStream(os);

        printHeaders();
        printPackage("entity." + subPackage);
        printImports();

        printClassHeader(className);
        startBlock();
        printFields();

        printAccessors();

        printToString();

        endBlock();
    }

    public void generateFetchOptions() throws FileNotFoundException
    {
        generateFetchOptions("source/java/ch/ethz/sis/openbis/generic/shared/api/v3/dto/fetchoptions/" + subPackage + "/" + className
                + "FetchOptions.java");
    }

    private void generateFetchOptions(String file) throws FileNotFoundException
    {
        PrintStream fos = new PrintStream(new FileOutputStream(file, false));
        try
        {
            generateFetchOptions(fos);
        } finally
        {
            fos.close();
        }
    }

    public void generateFetchOptions(PrintStream os)
    {
        setOutputStream(os);
        printHeaders();
        printPackage("fetchoptions." + subPackage);
        printImportsForFetchOptions();

        printClassHeader(fetchOptionsClass.getSimpleName());
        startBlock();

        printFetchOptionsFields();
        printFetchOptionsAccessors();

        endBlock();
    }

    private void printHeaders()
    {
        print("/*");
        print(" * Copyright 2014 ETH Zuerich, CISD");
        print(" *");
        print(" * Licensed under the Apache License, Version 2.0 (the \"License\");");
        print(" * you may not use this file except in compliance with the License.");
        print(" * You may obtain a copy of the License at");
        print(" *");
        print(" *      http://www.apache.org/licenses/LICENSE-2.0");
        print(" *");
        print(" * Unless required by applicable law or agreed to in writing, software");
        print(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
        print(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        print(" * See the License for the specific language governing permissions and");
        print(" * limitations under the License.");
        print(" */");

    }

    private void printAccessors()
    {
        for (DTOField field : fields)
        {
            printAccessors(field);
        }
    }

    private void printAccessors(DTOField field)
    {
        if (field.fetchOptions != null)
        {
            printGetterWithFetchOptions(field);
        }
        else
        {
            printBasicGetter(field);
        }

        printBasicSetter(field);
    }

    private void printFetchOptionsAccessors()
    {
        for (DTOField field : fields)
        {
            if (field.fetchOptions != null)
            {
                printFetchOptionsAccessors(field);
            }
        }
    }

    private void printBasicSetter(DTOField field)
    {
        print("public void set%s(%s %s)",
                field.getCapitalizedName(),
                field.definitionClassName,
                field.getPersistentName());
        startBlock();
        print("this.%s = %s;", field.getPersistentName(), field.getPersistentName());
        endBlock();
        print("");
    }

    private void printGetterWithFetchOptions(DTOField field)
    {
        print("@JsonIgnore");
        print("public %s get%s()", field.definitionClassName, field.getCapitalizedName());
        startBlock();
        print("if (getFetchOptions().has%s())", field.getCapitalizedName());
        startBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("else");
        startBlock();
        print("throw new NotFetchedException(\"%s has not been fetched.\");", field.description);
        endBlock();
        endBlock();
        print("");

    }

    private void printBasicGetter(DTOField field)
    {
        print("@JsonIgnore");
        if (field.definitionClassName.equals("Boolean"))
        {
            print("public %s is%s()", field.definitionClassName, field.getCapitalizedName());
        }
        else
        {
            print("public %s get%s()", field.definitionClassName, field.getCapitalizedName());
        }
        startBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("");
    }

    private void printFetchOptionsAccessors(DTOField field)
    {
        print("public %s fetch%s()", field.fetchOptions.getSimpleName(), field.getCapitalizedName());
        startBlock();
        print("if (%s == null)", field.getPersistentName());
        startBlock();
        print("%s = new %s();", field.getPersistentName(), field.fetchOptions.getSimpleName());
        endBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("");

        print("public %s fetch%s(%s fetchOptions)", field.fetchOptions.getSimpleName(), field.getCapitalizedName(),
                field.fetchOptions.getSimpleName());
        startBlock();
        print("return %s = fetchOptions;", field.getPersistentName());
        endBlock();
        print("");

        print("public boolean has%s()", field.getCapitalizedName());
        startBlock();
        print("return %s != null;", field.getPersistentName());
        endBlock();
        print("");

    }

    private void printClassHeader(String className)
    {
        print("/**");
        print(" * Class automatically generated with {@link %s}", this.getClass().getName());
        print(" */");
        print("@JsonObject(\"%s\")", className);
        print("public class %s implements Serializable", className);
    }

    private void printFields()
    {
        print("private static final long serialVersionUID = 1L;");
        print("");
        for (DTOField field : fields)
        {
            printField(field);
        }
    }

    private void printField(DTOField field)
    {
        printJsonPropertyAnnotation(field);
        print("private %s %s;", field.definitionClassName, field.getPersistentName());
        print("");
    }

    private void printFetchOptionsFields()
    {
        print("private static final long serialVersionUID = 1L;");
        print("");
        for (DTOField field : fields)
        {
            if (field.fetchOptions != null)
            {
                printFetchOptionField(field);
            }
        }
    }

    private void printFetchOptionField(DTOField field)
    {
        printJsonPropertyAnnotation(field);
        print("private %s %s;", field.fetchOptions.getSimpleName(), field.getPersistentName());
        print("");
    }

    protected void printJsonPropertyAnnotation(DTOField field)
    {
        if (field.fieldName.equalsIgnoreCase(field.getPersistentName()))
        {
            print("@JsonProperty");
        } else
        {
            print("@JsonProperty(value=\"%s\")", field.fieldName);
        }
    }

    private void printImports()
    {
        Set<String> imports = new TreeSet<String>();

        for (DTOField field : fields)
        {
            if (field.importClassName != null)
            {
                imports.add(field.importClassName);
            }
        }

        imports.addAll(additionalImports);

        for (String s : imports)
        {
            if (false == s.startsWith("java.lang"))
            {
                print("import %s;", s);
            }
        }
        print("");
    }

    private void printImportsForFetchOptions()
    {
        Set<String> imports = new TreeSet<String>();

        imports.add(JsonObject.class.getName());
        imports.add(JsonProperty.class.getName());
        imports.add(Serializable.class.getName());

        for (DTOField field : fields)
        {
            if (field.fetchOptions != null)
            {
                imports.add(field.fetchOptions.getName());
            }
        }

        for (String s : imports)
        {
            print("import %s;", s);
        }
        print("");
    }

    private void printPackage(String p)
    {
        print("package %s.%s;", PACKAGE_PREFIX, p);
        print("");
    }

    private void printToString()
    {
        if (toStringContent != null)
        {
            print("@Override");
            print("public String toString()");
            startBlock();
            print("return %s;", toStringContent);
            endBlock();
            print("");
        }
    }
}
