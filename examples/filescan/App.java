import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.trend.cloudone.amaas.AMaasClient;
import com.trend.cloudone.amaas.AMaasException;


public final class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    private App() {
    }

    private static void info(final String msg, final Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private static String[] listFiles(final String pathName) {
        File fObj = new File(pathName);
        if (!fObj.isDirectory()) {
            return new String[]{pathName};
        }
        return Stream.of(fObj.listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getPath)
            .collect(Collectors.toList()).toArray(new String[] {});
    }

    static void scanFilesInSequential(final AMaasClient client, final String[] fList, final String[] tagList, final boolean pmlFlag, final boolean feedbackFlag, final boolean verbose) {
        for (String fileName: fList) {
            try {
                info("===============> Scanning file {0}", fileName);
                long startTS = System.currentTimeMillis();
                String scanResult = client.scanFile(fileName, tagList, pmlFlag, feedbackFlag, verbose);
                long endTS = System.currentTimeMillis();
                info("{0}", scanResult);
                info("===============> File scan time {0}", endTS - startTS);
            } catch (AMaasException err) {
                info("Exception {0}", err.getMessage());
            }
        }
    }

    private static Options getCmdOptions() {
        Options optionList = new Options();
        optionList.addRequiredOption("f", "filename", true, "File path or folder to be scanned");
        optionList.addRequiredOption("k", "apikey", true, "Cloud One API key");
        optionList.addRequiredOption("r", "region", true, "AMaaS service region");
        optionList.addOption("t", "timeout", true, "Per scan timeout in seconds");
        optionList.addOption(null, "taglist", true, "commas separated string of tags.e.g, sdk,dev");
        optionList.addOption(null, "pml", true, "Enable predictive machine language detection");
        optionList.addOption(null, "feedback", true, "Enable Trend Smart Protection Network's Smart Feedback");
        optionList.addOption("v", "verbose", true, "Enable log verbose mode");
        return optionList;
    }

    /**
     * The program takes 4 options and respective values to configure the AMaaS SDK client.
     * @param args Input options:
     *                  -f a file or a directory to be scanned
     *                  -k the API key or bearer authentication token
     *                  -r region where the key/token was applied. eg, us-east-1
     *                  -t optional client maximum waiting time in seconds for a scan. 0 or missing means default.
     *                  --taglist a commas separated string of tags. e.g. dev,sdk
     *                  --pml enable predictive machine language detection. default to false
     *                  --feedback enable Trend Micro Smart Protection Network's Smart Feedback. default to false
     *                  -v enable log verbose mode. default to false
     */
    public static void main(final String[] args) {
        String pathname = "";
        String apikey = null;
        String region = "";
        long timeout = 0;
        String tags = null;
        boolean pmlFlag = false;
        boolean feedbackFlag = false;
        boolean verbose = false;

        DefaultParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        Options optionList = getCmdOptions();
        try {
            CommandLine cmd = parser.parse(optionList, args);
            if (cmd.hasOption("f")) {
                pathname = cmd.getOptionValue("f");
            }
            if (cmd.hasOption("r")) {
                apikey = cmd.getOptionValue("k");
            }
            if (cmd.hasOption("k")) {
                region = cmd.getOptionValue("r");
            }
            if (cmd.hasOption("t")) {
                timeout = Long.parseLong(cmd.getOptionValue("t"));
            }
            if (cmd.hasOption("taglist")) {
                tags = cmd.getOptionValue("taglist");
            }
            if (cmd.hasOption("pml")) {
                if (cmd.getOptionValue("pml").equals("true")) {
                    pmlFlag = true;
                }
            }
            if (cmd.hasOption("feedback")) {
                if (cmd.getOptionValue("feedback").equals("true")) {
                    feedbackFlag = true;
                }
            }
            if (cmd.hasOption("v")) {
                if (cmd.getOptionValue("v").equals("true")) {
                    verbose = true;
                }
            }
            String[] tagList = null;
            if (tags != null) {
                info("tags to used {0}", tags);
                tagList = tags.split(",");
            }

            AMaasClient client = new AMaasClient(region, apikey, timeout);
            String[] listOfFiles = listFiles(pathname);
            long totalStartTs = System.currentTimeMillis();

            scanFilesInSequential(client, listOfFiles, tagList, pmlFlag, feedbackFlag, verbose);

            long totalEndTs = System.currentTimeMillis();
            info("*************** Total scan time {0}", totalEndTs - totalStartTs);
        } catch (ParseException err) {
            helper.printHelp("Usage:", optionList);
        } catch (NumberFormatException err) {
            info("Exception parsing -t value must be a number");
        } catch (AMaasException err) {
            info("Exception encountered: {0}", err.getMessage());
        } catch (Exception err) {
            info("Unexpected exception encountered: {0}", err.getMessage());
        }
    }
}
