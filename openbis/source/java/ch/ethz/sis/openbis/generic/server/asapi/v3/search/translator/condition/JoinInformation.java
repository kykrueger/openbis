package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

public class JoinInformation {
    private String mainTable;
    private String mainTableId;
    private String mainTableAlias;
    private String subTable;
    private String subTableId;
    private String subTableAlias;

    public String getMainTable() {
        return mainTable;
    }

    public void setMainTable(String mainTable) {
        this.mainTable = mainTable;
    }

    public String getMainTableId() {
        return mainTableId;
    }

    public void setMainTableId(String mainTableId) {
        this.mainTableId = mainTableId;
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

    public String getSubTableId() {
        return subTableId;
    }

    public void setSubTableId(String subTableId) {
        this.subTableId = subTableId;
    }

    public String getSubTableAlias() {
        return subTableAlias;
    }

    public void setSubTableAlias(String subTableAlias) {
        this.subTableAlias = subTableAlias;
    }


}
