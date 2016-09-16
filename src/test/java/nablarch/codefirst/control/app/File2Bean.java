package nablarch.codefirst.control.app;

import javax.persistence.Entity;
import javax.persistence.Table;

import nablarch.common.databind.csv.Csv;
import nablarch.etl.WorkItem;

@Csv(
        type = Csv.CsvType.RFC4180,
        properties = {"name", "name2"}
)
@Entity
@Table(name = "file2_entity")
public class File2Bean extends WorkItem {

    private String name;
    private String name2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }
}
