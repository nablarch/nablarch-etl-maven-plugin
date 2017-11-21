package nablarch.codefirst.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nablarch.common.dao.DatabaseMetaDataExtractor;
import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.common.databind.csv.CsvFormat;
import nablarch.etl.WorkItem;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mockit.Expectations;
import mockit.Mocked;

/**
 * {@link ControlFileGenerator}のテストクラス。
 *
 * @author Naoki Yamamoto
 */
public class ControlFileGeneratorTest {

    /** テスト対象 */
    private static ControlFileGenerator sut = new ControlFileGenerator();

    BufferedReader reader;

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @After
    public void tearDown() throws Exception {
        if(reader != null){
            reader.close();
        }
    }

    /**
     * ファイルが生成されること。
     *
     * @throws Exception
     */
    @Test
    public void testExecute() throws Exception {
        File dir = temporaryFolder.newFolder();
        sut.execute(Person.class, "template/template.ftl" , dir);

        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir, "Person.ctl")), "UTF-8"));
        assertThat(reader.readLine(), is("OPTIONS ("));
        assertThat(reader.readLine(), is("SKIP = 1,"));
        assertThat(reader.readLine(), is("DIRECT = TRUE"));
        assertThat(reader.readLine(), is(")"));
        assertThat(reader.readLine(), is("LOAD DATA"));
        assertThat(reader.readLine(), is("CHARACTERSET AL32UTF8"));
        assertThat(reader.readLine(), is("INFILE '' \"str '\\r\\n'\""));
        assertThat(reader.readLine(), is("TRUNCATE"));
        assertThat(reader.readLine(), is("PRESERVE BLANKS"));
        assertThat(reader.readLine(), is("INTO TABLE PERSON"));
        assertThat(reader.readLine(), is("FIELDS"));
        assertThat(reader.readLine(), is("TERMINATED BY ','"));
        assertThat(reader.readLine(), is("OPTIONALLY ENCLOSED BY '\"'"));
        assertThat(reader.readLine(), is("TRAILING NULLCOLS"));
        assertThat(reader.readLine(), is("("));
        assertThat(reader.readLine(), is("LINE_NUMBER RECNUM,"));
        assertThat(reader.readLine(), is("PERSON_ID,"));
        assertThat(reader.readLine(), is("AGE,"));
        assertThat(reader.readLine(), is("NAME"));
        assertThat(reader.readLine(), is(")"));
    }

    /**
     * Bean定義に合わせてファイルが生成されること。
     *
     * @throws Exception
     */
    @Test
    public void testExecute_bean() throws Exception {
        File dir = temporaryFolder.newFolder();
        sut.execute(PersonCustom.class, "template/template.ftl" , dir);

        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir, "PersonCustom.ctl")), "UTF-8"));
        assertThat(reader.readLine(), is("OPTIONS ("));
        assertThat(reader.readLine(), is("DIRECT = TRUE"));
        assertThat(reader.readLine(), is(")"));
        assertThat(reader.readLine(), is("LOAD DATA"));
        assertThat(reader.readLine(), is("CHARACTERSET JA16SJISTILDE"));
        assertThat(reader.readLine(), is("INFILE '' \"str '\\n'\""));
        assertThat(reader.readLine(), is("TRUNCATE"));
        assertThat(reader.readLine(), is("PRESERVE BLANKS"));
        assertThat(reader.readLine(), is("INTO TABLE CUSTOM.PERSON_CUSTOM"));
        assertThat(reader.readLine(), is("FIELDS"));
        assertThat(reader.readLine(), is("TERMINATED BY '\\t'"));
        assertThat(reader.readLine(), is("OPTIONALLY ENCLOSED BY '\''"));
        assertThat(reader.readLine(), is("TRAILING NULLCOLS"));
        assertThat(reader.readLine(), is("("));
        assertThat(reader.readLine(), is("LINE_NUMBER RECNUM,"));
        assertThat(reader.readLine(), is("PERSON_ID,"));
        assertThat(reader.readLine(), is("AGE,"));
        assertThat(reader.readLine(), is("NAME"));
        assertThat(reader.readLine(), is(")"));
    }

    /**
     * テンプレート定義に合わせてファイルが生成されること。
     *
     * @throws Exception 例外
     */
    @Test
    public void testExecute_template() throws Exception {
        File dir = temporaryFolder.newFolder();
        sut.execute(Person.class, "template/template_custom.ftl" , dir);

        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir, "Person.ctl")), "UTF-8"));
        assertThat(reader.readLine(), is("OPTIONS ("));
        assertThat(reader.readLine(), is("SKIP = 1,"));
        assertThat(reader.readLine(), is(")"));
        assertThat(reader.readLine(), is("LOAD DATA"));
        assertThat(reader.readLine(), is("CHARACTERSET AL32UTF8"));
        assertThat(reader.readLine(), is("REPLACE"));
        assertThat(reader.readLine(), is("PRESERVE BLANKS"));
        assertThat(reader.readLine(), is("INTO TABLE PERSON"));
        assertThat(reader.readLine(), is("FIELDS"));
        assertThat(reader.readLine(), is("TERMINATED BY ','"));
        assertThat(reader.readLine(), is("OPTIONALLY ENCLOSED BY '\"'"));
        assertThat(reader.readLine(), is("TRAILING NULLCOLS"));
        assertThat(reader.readLine(), is("("));
        assertThat(reader.readLine(), is("LINE_NUMBER RECNUM,"));
        assertThat(reader.readLine(), is("PERSON_ID,"));
        assertThat(reader.readLine(), is("AGE,"));
        assertThat(reader.readLine(), is("NAME"));
        assertThat(reader.readLine(), is(")"));
    }

    /**
     * テンプレートファイルのフォーマットが不正な場合に例外を送出すること。
     *
     * @throws Exception 例外
     */
    @Test
    public void testExecute_template_invalid() throws Exception {

        File dir = temporaryFolder.newFolder();

        try {
            sut.execute(Person.class, "template/template_invalid.ftl", dir);
            fail("テンプレートファイルのフォーマット不正のため例外が発生。");
        } catch (InvalidTemplateFormatException e) {
            assertThat(e.getMessage(), is("template file format is invalid. template file = [template/template_invalid.ftl]"));
        }
    }

    /**
     * コントロールファイルの生成に失敗した場合に例外を送出すること。
     *
     * @throws Exception 例外
     */
    @Test
    public void testExecute_generate_failed() throws Exception {
        File dir = temporaryFolder.newFolder();
        try {
            sut.execute(Person.class, "notfound\\template.ftl", dir);
            fail("コントロールファイルの生成に失敗したため例外が発生。");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("failed to generate control file."));
        }
    }

    /**
     * WorkItemを継承しないBeanが指定された場合に例外を送出すること。
     *
     * @throws Exception 例外
     */
    @Test
    public void testExecute_bean_not_extends_work_item() throws Exception {
        File dir = temporaryFolder.newFolder();
        try {
            sut.execute(PersonNotExtendsWorkItem.class, "template/template.ftl", dir);
            fail("WorkItemを継承していないBeanが指定されたため、例外が発生。");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("beanClass should extend nablarch.etl.WorkItem"));
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"personId", "age", "name"}, headers = {"ID", "年齢", "氏名"})
    @Entity
    public static class Person extends WorkItem {
        public Long personId;
        public Long age;
        public String name;

        @Column(name = "PERSON_ID", length = 9, nullable = false, unique = true)
        public Long getPersonId() {
            return personId;
        }

        public void setPersonId(Long personId) {
            this.personId = personId;
        }

        @Column(name = "AGE", length = 3, nullable = true, unique = false)
        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }

        @Column(name = "NAME", length = 256, nullable = true, unique = false)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.CUSTOM, properties = {"personId", "age", "name"}, headers = {"ID", "年齢", "氏名"})
    @CsvFormat(
            quote = '\'',
            charset = "MS932",
            fieldSeparator = '\t',
            ignoreEmptyLine = true,
            lineSeparator = "\n",
            quoteMode = CsvDataBindConfig.QuoteMode.ALL,
            requiredHeader = false,
            emptyToNull = false)
    @Entity
    @Table(name = "PERSON_CUSTOM", schema = "CUSTOM")
    public static class PersonCustom extends WorkItem {
        public Long personId;
        public Long age;
        public String name;

        @Column(name = "PERSON_ID", length = 9, nullable = false, unique = true)
        public Long getPersonId() {
            return personId;
        }

        public void setPersonId(Long personId) {
            this.personId = personId;
        }

        @Column(name = "AGE", length = 3, nullable = true, unique = false)
        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }

        @Column(name = "NAME", length = 256, nullable = true, unique = false)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Csv(type = Csv.CsvType.DEFAULT, properties = {"personId", "age", "name"}, headers = {"ID", "年齢", "氏名"})
    @Entity
    public static class PersonNotExtendsWorkItem {
        public Long personId;
        public Long age;
        public String name;

        @Column(name = "PERSON_ID", length = 9, nullable = false, unique = true)
        public Long getPersonId() {
            return personId;
        }

        public void setPersonId(Long personId) {
            this.personId = personId;
        }

        @Column(name = "AGE", length = 3, nullable = true, unique = false)
        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }

        @Column(name = "NAME", length = 256, nullable = true, unique = false)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
