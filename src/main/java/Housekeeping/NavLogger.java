package Housekeeping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NavLogger {
    public static void logInfo(String message){
        Logger logger= LogManager.getLogger();
        logger.info(message);
    }

    public static void logSuccess(String message){
        Logger logger= LogManager.getLogger();
        logger.info("SUCCESS: "+message);
    }

    public static void logWarning(String message){
        Logger logger= LogManager.getLogger();
        logger.warn(message);
    }

    public static void logError(String message){
        Logger logger= LogManager.getLogger();
        logger.error(message);
    }

    public static void logFatal(String message){
        Logger logger= LogManager.getLogger();
        logger.fatal(message);
    }
}
