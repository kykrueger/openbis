package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

public class JoinInformation
{

    private String mainTable;

    private String mainTableIdField;

    private String mainTableAlias;

    private String subTable;

    private String subTableIdField;

    private String subTableAlias;

    public String getMainTable() {
        return mainTable;
    }

    public void setMainTable(String mainTable) {
        this.mainTable = mainTable;
    }

    public String getMainTableIdField() {
        return mainTableIdField;
    }

    public void setMainTableIdField(String mainTableIdField) {
        this.mainTableIdField = mainTableIdField;
    }

    public String getMainTableAlias() {
        return mainTableAlias;
    }

    public void setMainTableAlias(String mainTableAlias) {
        this.mainTableAlias = mainTableAlias;
    }

    public String getSubTable() {
        return subTable;
    }

    public void setSubTable(String subTable) {
        this.subTable = subTable;
    }

    public String getSubTableIdField() {
        return subTableIdField;
    }

    public void setSubTableIdField(String subTableIdField) {
        this.subTableIdField = subTableIdField;
    }

    public String getSubTableAlias() {
        return subTableAlias;
    }

    public void setSubTableAlias(String subTableAlias) {
        this.subTableAlias = subTableAlias;
    }

}
