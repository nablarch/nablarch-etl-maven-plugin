package nablarch.codefirst.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import nablarch.common.dao.ColumnMeta;
import nablarch.common.dao.EntityUtil;
import nablarch.common.databind.DataBindUtil;
import nablarch.common.databind.csv.Csv;
import nablarch.common.databind.csv.CsvDataBindConfig;
import nablarch.core.util.FileUtil;
import nablarch.etl.WorkItem;

/**
 * SQL*Loaderで使用するコントロールファイルの生成を行うクラス。
 *
 * @author Naoki Yamamoto
 */
public class ControlFileGenerator {

    /** 文字コードのマッピングを定義したプロパティファイル名 */
    private static final String PROPERTIES_FILE_NAME = "charset.properties";

    /**
     * BeanクラスとテンプレートファイルからSQL*Loaderで使用するコントロールファイルを作成し、
     * 指定された出力先ディレクトリに配置する。
     *
     * @param beanClass Beanクラス
     * @param templateFile テンプレートファイル
     * @param outputDir 出力先のディレクトリ
     * @param <T> 総称型
     */
    public <T> void execute(Class<T> beanClass, String templateFile, File outputDir) {

        if(!(WorkItem.class.isAssignableFrom(beanClass))){
            // ETLで使うbeanclassはnablarch.etl.WorkItemを継承するべき
            throw new IllegalStateException("beanClass should extend " + WorkItem.class.getName());
        }

        ControlDefinition control = createControlDefinition(beanClass);

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputDir, beanClass.getSimpleName() + ".ctl")), Charset.forName("UTF-8")));
            Template template = createTemplate(templateFile);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("control", control);
            template.process(params, writer);
        } catch (TemplateException e) {
            throw new InvalidTemplateFormatException("template file format is invalid. template file = [" + templateFile + ']', e);
        } catch (IOException e) {
            throw new RuntimeException("failed to generate control file.", e);
        } finally {
            FileUtil.closeQuietly(writer);
        }
    }

    /**
     * テンプレートファイルから{@link Template}を生成する。
     * @param templateFile テンプレートファイル
     * @return 指定したテンプレートファイルの{@link Template}オブジェクト
     * @throws IOException 入出力例外
     */
    private Template createTemplate(String templateFile) throws IOException {
        Configuration config = new Configuration(Configuration.VERSION_2_3_22);
        config.setEncoding(Locale.getDefault(), "UTF-8");
        config.setTemplateLoader(new ClassTemplateLoader(getClass(), ""));
        return config.getTemplate(templateFile);
    }

    /**
     * Beanクラスの定義から{@link ControlDefinition}を生成する。
     *
     * @param beanClass Beanクラス
     * @param <T> 総称型
     * @return Beanクラスから生成した{@link ControlDefinition}オブジェクト
     */
    private <T> ControlDefinition createControlDefinition(Class<T> beanClass) {
        ControlDefinition control = new ControlDefinition();
        CsvDataBindConfig config = (CsvDataBindConfig) DataBindUtil.createDataBindConfig(beanClass);
        control.setTableName(EntityUtil.getTableNameWithSchema(beanClass));
        control.setCharset(convertCharset(config.getCharset().name()));
        control.setRequiredHeader(config.isRequiredHeader());
        control.setQuote(escape(String.valueOf(config.getQuote())));
        control.setFieldSeparator(escape(String.valueOf(config.getFieldSeparator())));
        control.setLineSeparator(escape(config.getLineSeparator()));
        control.setColumnNames(getColumnNames(beanClass));

        return control;
    }

    /**
     * 引数で受け取った文字コードを、クラスパス配下に格納された"charset.properties"を利用して
     * Oracle用の文字コードに変換する。
     *
     * @param charset 文字コード
     * @return 変換後文字コード
     */
    private String convertCharset(String charset) {
        Properties properties = new Properties();
        InputStream is = FileUtil.getResource("classpath:" + PROPERTIES_FILE_NAME);
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtil.closeQuietly(is);
        }
        return properties.getProperty(charset);
    }


    /**
     * Beanに定義されているカラム名のリストを取得する。
     *
     * @param beanClass Beanクラス
     * @param <T> 総称型
     * @return カラム名のリスト
     */
    private <T> List<String> getColumnNames(Class<T> beanClass) {
        List<ColumnMeta> columns = EntityUtil.findAllColumns(beanClass);
        Map<String, String> columnNameMap = new HashMap<String, String>();
        List<String> fieldNames = new ArrayList<String>();

        String lineNumberPropertyName = DataBindUtil.findLineNumberProperty(beanClass);

        for (ColumnMeta column : columns) {
            if(column.getPropertyName().equals(lineNumberPropertyName)){
                fieldNames.add(column.getName() + " RECNUM");
            }else{
                columnNameMap.put(column.getPropertyName(), column.getName());
            }
        }

        String[] properties = beanClass.getAnnotation(Csv.class).properties();
        for (String property : properties) {
            fieldNames.add(columnNameMap.get(property));
        }
        return fieldNames;
    }

    /**
     * 文字列中に含まれる改行コードやタブをエスケープする。
     * @param str 文字列
     * @return エスケープ後の文字列
     */
    private String escape(String str) {
        return str.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
    }
}
