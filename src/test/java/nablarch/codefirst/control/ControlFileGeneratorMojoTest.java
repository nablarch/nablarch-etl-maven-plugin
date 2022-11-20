package nablarch.codefirst.control;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.hamcrest.CoreMatchers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import mockit.Mocked;

/**
 * {@link ControlFileGeneratorMojo}のテストクラス。
 */
public class ControlFileGeneratorMojoTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final ControlFileGeneratorMojo sut = new ControlFileGeneratorMojo();
    
    @Mocked
    private MavenProject mockProject;
    
    @Before
    public void setUp() throws Exception {
        sut.templateFilePath = "template/template.ftl";
        sut.outputPath = folder.getRoot();
        sut.project = mockProject;
    }

    /**
     * 標準テンプレート(提供するテンプレート)を使用してファイルが生成できること
     */
    @Test
    public void testDefaultFormat() throws Exception {
        sut.classes = new ArrayList<String>() {{
            add("nablarch.codefirst.control.app.File1Bean");
        }};

        sut.execute();

        final File[] files = folder.getRoot().listFiles();
        assertThat("1ファイル出力されること", files.length, is(1));
        assertThat("ファイル名はBeanクラス名.ctl", files[0].getName(), is("File1Bean.ctl"));

        final String result = readFile(files[0]);
        System.out.println("result = " + result);

        final List<String> expected = new ArrayList<String>();
        expected.add("OPTIONS (");
        expected.add("SKIP = 1");                       // ヘッダー有りなので先頭行はスキップ
        expected.add("DIRECT = TRUE");                  // ダイレクトパスインサートモード
        expected.add(")");
        expected.add("LOAD DATA");
        expected.add("CHARACTERSET AL32UTF8");
        expected.add("INFILE '' \"str '\\r\\n'\"");     // 改行コード
        expected.add("INTO TABLE file_bean");
        expected.add("FIELDS");
        expected.add("TERMINATED BY ','");              // カンマ区切り
        expected.add("OPTIONALLY ENCLOSED BY '\"'");    // ダブルクォートで囲まれる
        expected.add("TRAILING NULLCOLS");
        expected.add("LINE_NUMBER RECNUM");             // 行番号を登録する
        expected.add("NAME");
        assertThat(result, stringContainsInOrder(expected));
    }

    /**
     * 複数のBeanを指定した場合でも正しく生成できること。
     *
     * コントロールファイルの生成部のテストに関しては、{@link ControlFileGeneratorTest}で実施されているため、
     * このテストではテンプレートを置き換えてファイルが想定通り出力されていることのみ確認する。
     */
    @Test
    public void testMultipleFile() throws Exception {
        sut.templateFilePath = "template/mojo_test.ftl";
        sut.classes = new ArrayList<String>() {{
            add("nablarch.codefirst.control.app.File1Bean");
            add("nablarch.codefirst.control.app.File2Bean");
        }};

        sut.execute();

        final File[] list = folder.getRoot().listFiles();
        assertThat("2ファイル生成される", list.length, is(2));

        Map<String, String> expected = new HashMap<String, String>() {{
            put("File1Bean.ctl", "file_bean");
            put("File2Bean.ctl", "file2_entity");
        }};
        for (File file : list) {
            final String tableName = expected.get(file.getName());
            final String actual = readFile(file);
            assertThat(actual, containsString("table=" + tableName));
        }
    }

    /**
     * 指定したクラスが存在しない場合、{@link IllegalArgumentException}が送出されること。
     */
    @Test
    public void testClassNotFound() throws Exception {
        // -------------------------------------------------- setup expected exeption
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                CoreMatchers.is("指定されたBeanクラスが存在しません。"
                        + " Beanクラス=[nablarch.codefirst.control.app.File1BeanNotFound]"));


        // -------------------------------------------------- execute
        sut.classes = new ArrayList<String>() {{
            add("nablarch.codefirst.control.app.File1BeanNotFound");
        }};
        sut.execute();
    }

    private String readFile(File file) throws Exception {
        final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
        final ByteArrayOutputStream output = new ByteArrayOutputStream(512);
        byte[] b = new byte[1024];
        int length;
        while ((length = stream.read(b)) != -1) {
            output.write(b, 0, length);
        }
        return output.toString("utf-8");
    }
}
