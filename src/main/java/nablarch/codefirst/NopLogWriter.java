package nablarch.codefirst;

import nablarch.core.log.basic.LogContext;
import nablarch.core.log.basic.LogWriter;
import nablarch.core.log.basic.ObjectSettings;

/**
 * 何もしない{@link LogWriter}実装クラス。
 *
 * @author Hisaaki Shioiri
 */
public class NopLogWriter implements LogWriter {

    @Override
    public void initialize(ObjectSettings settings) {

    }

    @Override
    public void terminate() {

    }

    @Override
    public void write(LogContext context) {

    }
}
