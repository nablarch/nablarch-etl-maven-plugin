package nablarch.codefirst.control;

/**
 * テンプレートファイルのフォーマットが不正な場合に送出される例外クラス。
 *
 * @author Naoki Yamamoto
 */
public class InvalidTemplateFormatException extends RuntimeException {

    /**
     * 例外を送出する。
     *
     * @param message 例外メッセージ
     * @param cause 原因
     */
    public InvalidTemplateFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
