import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.ResponseInputStream;

import com.trend.cloudone.amaas.AMaasClient;
import com.trend.cloudone.amaas.AMaasException;

public class S3App {
    private static final Logger logger = Logger.getLogger(S3App.class.getName());

    private static void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }
 
    public static byte[] serialize(ResponseInputStream<GetObjectResponse> data) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
        int nRead;
        byte[] byteArray = new byte[16384];
    
        try {
            while ((nRead = data.read(byteArray, 0, byteArray.length)) != -1) {
                buffer.write(byteArray, 0, nRead);
            }
        } catch (IOException e) {
            info("I/O error while serializing data: {} | {}", e.getMessage(), e);
        }
    
        return buffer.toByteArray();
    }

    public static byte[] downloadS3Object(String regionstr, String bucketName, String key) throws Exception {
        
        S3Client s3 = S3Client.builder()
            .credentialsProvider(ProfileCredentialsProvider.create())
            .region(Region.of(regionstr))
            .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        ResponseInputStream<GetObjectResponse> response = s3.getObject(getObjectRequest);
        return serialize(response);
    }

    private static Options getCmdOptions() {
        Options optionList = new Options();
        optionList.addRequiredOption("a", "awsregion", true, "AWS region");
        optionList.addRequiredOption("b", "bucket", true, "S3 bucket name");
        optionList.addRequiredOption("f", "S3key", true, "S3 key to be scanned");
        optionList.addRequiredOption("k", "apikey", true, "Cloud One API key");
        optionList.addRequiredOption("r", "region", true, "AMaaS service region");
        optionList.addOption("t", "timeout", true, "Per scan timeout in seconds");
        return optionList;
    }

    /*
      * The program takes 6 options and respecive values to configure the AMaaS SDK client.
      * @param String[]  Input options:
      *                  -a AWS region 
      *                  -b S3 bucket name
      *                  -f S3 key to be scanned
      *                  -k the API key or bearer authentication token
      *                  -r region where the C1 key/token was applied. eg, us-east-1
      *                  -t optional client maximum waiting time in seconds for a scan. 0 or missing means default.
    */
    public static void main(String[] args) {
        String awsRegion = "";
        String bucketName = "";
        String keyName = "";
        String apikey = null;
        String amaasRegion = "";
        long timeout = 0;

        DefaultParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        Options optionList = getCmdOptions();
        try {
            CommandLine cmd = parser.parse(optionList, args);
            if (cmd.hasOption("a")) {
                awsRegion = cmd.getOptionValue("a");
            }
            if (cmd.hasOption("b")) {
                bucketName = cmd.getOptionValue("b");
            } 
            if (cmd.hasOption("f")) {
                keyName = cmd.getOptionValue("f");
            }
            if (cmd.hasOption("r")) {
                apikey = cmd.getOptionValue("k");
            }
            if (cmd.hasOption("k")) {
                amaasRegion = cmd.getOptionValue("r");
            }
            if (cmd.hasOption("t")) {
                timeout = Long.parseLong(cmd.getOptionValue("t"));
            }
        
            info("Downloading S3 Object....");
            byte[] bytes = downloadS3Object(awsRegion, bucketName, keyName);
            info("Completed downloading S3 Object....");
            AMaasClient client = new AMaasClient(amaasRegion, apikey, timeout, true);
            long totalStartTs = System.currentTimeMillis();
            client.scanBuffer(bytes, keyName);
            
            long totalEndTs = System.currentTimeMillis();
            info("*************** Total scan time {0}", totalEndTs - totalStartTs);
        } catch (ParseException err) {
            helper.printHelp("Usage:", optionList);
        } catch (NumberFormatException err) {
            info("Exception parsing -t value must be a number");
        } catch (AMaasException err) {
            info("AMaaS SDK Exception encountered: {0}", err.getMessage());
        } catch (Exception err) {
            info("Unexpected exception encountered: {0}", err.getMessage());
        }
    }
}
