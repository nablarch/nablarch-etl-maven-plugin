package nablarch.codefirst.control;

import java.util.ArrayList;
import java.util.List;

/**
 * コントロールファイルの生成に必要な設定を保持するクラス。
 *
 * @author Naoki Yamamoto
 */
public class ControlDefinition {

    /** ヘッダ有無 */
    private boolean requiredHeader;

    /** 文字コード */
    private String charset;

    /** テーブル名 */
    private String tableName;

    /** クォート */
    private String quote;

    /** 列区切り文字 */
    private String fieldSeparator;

    /** 行区切り文字 */
    private String lineSeparator;

    /** カラム名のリスト */
    private List<String> columnNames = new ArrayList<String>();

    /**
     * ヘッダ有無を取得する。
     * @return ヘッダ有無
     */
    public boolean isRequiredHeader() {
        return requiredHeader;
    }

    /**
     * ヘッダ有無を設定する。
     * @param requiredHeader ヘッダ有無
     */
    public void setRequiredHeader(boolean requiredHeader) {
        this.requiredHeader = requiredHeader;
    }

    /**
     * 文字コードを取得する。
     * @return 文字コード
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 文字コードを設定する。
     * @param charset 文字コード
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * テーブル名を取得する。
     * @return テーブル名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * テーブル名を設定する。
     * @param tableName テーブル名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * クォートを取得する。
     * @return クォート
     */
    public String getQuote() {
        return quote;
    }

    /**
     * クォートを設定する。
     * @param quote クォート
     */
    public void setQuote(String quote) {
        this.quote = quote;
    }

    /**
     * 列区切り文字を取得する。
     * @return 列区切り文字
     */
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * 列区切り文字を設定する。
     * @param fieldSeparator 列区切り文字
     */
    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * 行区切り文字を取得する。
     * @return 行区切り文字
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * 行区切り文字を設定する。
     * @param lineSeparator 行区切り文字
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /**
     * カラム名のリストを取得する。
     * @return カラム名のリスト
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * カラム名のリストを設定する。
     * @param columnNames カラム名のリスト
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
}
