package nablarch.codefirst.control.app;

import javax.persistence.Entity;
import javax.persistence.Table;

import nablarch.common.databind.csv.Csv;
import nablarch.etl.WorkItem;

@Csv(
        type = Csv.CsvType.DEFAULT,
        properties = {"name"},
        headers = {"なまえ"}
)
@Entity
@Table(name = "file_bean")
public class File1Bean extends WorkItem {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
