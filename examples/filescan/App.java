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


public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    private static void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    public static String[] listFiles(String pathName) {
        File fObj = new File(pathName);
        if (!fObj.isDirectory()) {
            return new String[]{pathName};
        }
        return Stream.of(fObj.listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getPath)
            .collect(Collectors.toList()).toArray(new String[] {});
    }

    static void scanFilesInSequential(AMaasClient client, String[] fList) {
        for (String fileName: fList) {
            try {
                info("===============> Scanning file {0}", fileName);
                long startTS = System.currentTimeMillis();
                String scanResult = client.scanFile(fileName);
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
        return optionList;
    }
      
    /*
     * The program takes 4 options and respecive values to configure the AMaaS SDK client.
     * @param String[]  Input options:
     *                  -f a file or a directory to be scanned
     *                  -k the API key or bearer authentication token
     *                  -r region where the key/token was applied. eg, us-east-1
     *                  -t optional client maximum waiting time in seconds for a scan. 0 or missing means default.
     */
    public static void main(String[] args) {
        String pathname = "";
        String apikey = null;
        String region = "";
        long timeout = 0;

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

            AMaasClient client = new AMaasClient(region, apikey, timeout);
            String[] listOfFiles = listFiles(pathname);
            long totalStartTs = System.currentTimeMillis();
        
            scanFilesInSequential(client, listOfFiles);
        
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
