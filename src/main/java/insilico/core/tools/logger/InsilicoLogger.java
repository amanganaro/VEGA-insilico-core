package insilico.core.tools.logger;

import insilico.core.exception.InitFailureException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.net.URL;


/**
 * Provides method to init logger (should be invoked when application start)
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InsilicoLogger {

    private static final String CFG_URL = "/insilico/insilico.core/tools/logger/logger_default.cfg";
    private static final String CFG_DEBUG_URL = "/insilico/insilico.core/tools/logger/logger_debug.cfg";
    private static final String CFG_OFF_URL = "/insilico/insilico.core/tools/logger/logger_off.cfg";

    /**
     * Initialize logger for default application.
     *
     * @throws InitFailureException
     */
    public static void InitLogger() throws InitFailureException {
        try {
            URL u = Object.class.getResource(CFG_URL);
            PropertyConfigurator.configure(u);
        } catch (Throwable e) {
            throw new InitFailureException("Unable to init logger");
        }
    }

    /**
     * Initialize logger for debugging purposes.
     *
     * @throws InitFailureException
     */
    public static void InitLoggerForDebug() throws InitFailureException {
        try {
            URL u = Object.class.getResource(CFG_DEBUG_URL);
            PropertyConfigurator.configure(u);
            getLogger().debug("Starting insilico.core in debug mode");
        } catch (Throwable e) {
            throw new InitFailureException("Unable to init logger");
        }
    }

    /**
     * Initialize logger in disabled mode (no logging needed)
     *
     * @throws InitFailureException
     */
    public static void InitLoggerOff() throws InitFailureException {
        try {
            URL u = Object.class.getResource(CFG_OFF_URL);
            PropertyConfigurator.configure(u);
        } catch (Throwable e) {
            throw new InitFailureException("Unable to init logger");
        }
    }


    /**
     * Return the logger object
     *
     * @return Log4j logger static object
     */
    public static Logger getLogger() {
        return Logger.getRootLogger();
    }

}
