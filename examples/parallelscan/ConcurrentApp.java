import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
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

public final class ConcurrentApp {
    private static final Logger logger = Logger.getLogger(ConcurrentApp.class.getName());
    private static final int MAX_NUM_OF_THREADS = 5;
    private static final int MILLISEC_PER_SEC = 1000;
    private static final int DELAY_MILLISEC = 500;

    private ConcurrentApp() {
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

    /*
     * Value object class for tracking a scan result.
     */
    private static final class ScanResult {
        private String scanResult;
        private long scanTime;
        ScanResult(final String result, final long scanTime) {
            this.scanResult = result;
            this.scanTime = scanTime;
        }

        @Override
        public String toString() {
            return scanResult + " " + scanTime;
        }
    }

    /*
     * Task for executing a file scan.
     */
    private static final class Task implements Callable<ScanResult> {
        private final String fileName;
        private final AMaasClient client;

        Task(final AMaasClient client, final String fileName) {
            this.fileName = fileName;
            this.client = client;
        }

        @Override
        public ScanResult call() throws Exception {
            ScanResult result = null;
            try {
                long startTS = System.currentTimeMillis();
                String scanResult = client.scanFile(this.fileName);
                long endTS = System.currentTimeMillis();
                if (scanResult != null) {
                    result = new ScanResult(scanResult, endTS - startTS);
                }
            } catch (AMaasException err) {
                info("Scan file execption {0} {1}", fileName, err.getMessage());
            }
            return result;
        }

    }

    static void scanFilesInParallel(final AMaasClient client, final String[] fList, final long timeout)  {
        info("Scan files in Parallel");
        int numThreads = MAX_NUM_OF_THREADS;
        if (fList.length < numThreads) {
            numThreads = fList.length;
        }
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(numThreads);
            CompletionService<ScanResult> scanService = new ExecutorCompletionService<>(executor);
            for (String file : fList) {
                Task task = new Task(client, file);
                scanService.submit(task);
            }

            for (int i = 0; i < fList.length; i++) {
                Future<ScanResult> future = scanService.take();
                ScanResult result = future.get(timeout * MILLISEC_PER_SEC + DELAY_MILLISEC, TimeUnit.MILLISECONDS);
                if (result != null) {
                    info(future.get().toString());
                }
            }
        } catch (NullPointerException err) {
            info("Unexpected null executor exception: {0}", err.getMessage());
        } catch (ExecutionException | InterruptedException | CancellationException | TimeoutException err) {
            info("Concurrent execution encoutered exception: {0}", err.getMessage());
        } finally {
            if (executor != null) {
                executor.shutdown(); //always reclaim resources
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

    /**
    * The program takes 4 options and respective values to configure the AMaaS SDK client.
    * @param args Input options:
    *                  -f a file or a directory to be scanned
    *                  -k the API key or bearer authentication token
    *                  -r region where the key/token was applied. eg, us-east-1
    *                  -t optional client maximum waiting time in seconds for a scan. 0 or missing means default.
    */
    public static void main(final String[] args) {
        String pathName = "";
        String apikey = null;
        String region = "";
        long timeout = 0;

        DefaultParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        Options optionList = getCmdOptions();
        try {
            CommandLine cmd = parser.parse(optionList, args);
            if (cmd.hasOption("f")) {
                pathName = cmd.getOptionValue("f");
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
            String[] listOfFiles = listFiles(pathName);
            long totalStartTs = System.currentTimeMillis();
            scanFilesInParallel(client, listOfFiles, timeout);

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
