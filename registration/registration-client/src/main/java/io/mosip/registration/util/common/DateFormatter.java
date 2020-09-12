package io.mosip.registration.util.common;

import gnu.trove.impl.sync.TSynchronizedShortObjectMap;
import io.mosip.kernel.auth.adapter.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Format String date to desired format
 *
 * @author CONDEIS
 *
 */
public class DateFormatter {
    private static Logger log = LoggerConfiguration.logConfig(DateFormatter.class);
    private static String DEFAULT_INPUT_DATE_FORMAT = "yyyy-MM-dd";
    private static String DEFAULT_OUPUT_DATE_FORMAT = "dd/MM/yyyy";
    private static String DEFAULT_LONG_OUTPUT_DATE_FORMAT = "EEEEE dd MMMMM yyyy";

    /**
     * Handle date formatting
     *
     * @param date in format "yyyy-MM-dd"
     * @return formatted date in dd/MM/yyyy
     */
    public static String formatDate(String date) {

        return dateFormatter(date, DEFAULT_INPUT_DATE_FORMAT, DEFAULT_OUPUT_DATE_FORMAT);

    }

    /**
     * Handle date formatting
     *
     * @param date in format "yyyy-MM-dd"
     * @return formatted date EEEEE dd MMMMM yyyy
     */

    public static String formatLongDate(String date) {
        return dateFormatter(date, DEFAULT_INPUT_DATE_FORMAT, DEFAULT_LONG_OUTPUT_DATE_FORMAT);

    }

    /**
     * Handles date formatting
     *
     * @param date
     * @param inpuFormat
     * @param outputFormat
     * @return a formatted date
     */
    public static String formatDate(String date, String inpuFormat, String outputFormat) {

        return dateFormatter(date, inpuFormat, outputFormat);

    }

    /**
     * Handles date formatting using default output format defined
     *
     * @param date
     * @param inpuFormat
     * @return a formatted date
     */
    public static String formatDate(String date, String inpuFormat) {
        return dateFormatter(date, inpuFormat, DEFAULT_OUPUT_DATE_FORMAT);

    }
    public static String formatDateByReplacement (String date)
    {
        return date.replace("-", "/");
    }
    private static String dateFormatter(String dateS, String inpuFormat, String outputFormat) {
        String dateFormatted = dateS;

        try {
            SimpleDateFormat provided = new SimpleDateFormat(inpuFormat);
            SimpleDateFormat expected = new SimpleDateFormat(outputFormat);
            Date date = provided.parse(dateS);
            dateFormatted = expected.format(date);

        } catch (Exception e) {
            log.error("sessionId", "idType", "Unable to format date", dateFormatted);
        }
        return dateFormatted;

    }
}
