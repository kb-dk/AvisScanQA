package dk.kb.kula190.cli;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class MainInvoker {


    //    private static final File
//            specificBatch
//            = new File(System.getenv("HOME") + "/AvisScanQA_cli/lastTIme/data2/freja1849_18491022_18501231_RT1");
//    private static final File
//            specificBatch
//            = new File(System.getenv("HOME") + "/Documents/ribesocialdemokrat_19100101_19100531_RT2");
    private static final File
            specificBatch
            = new File(System.getenv("HOME") + "/Documents/dybboelposten_19070701_19071231_RT1");
//    private static final File
//            specificBatch
//            = new File(System.getenv("HOME") + "/Documents/this is a + test folder/schleswigschegrenzpost_18650103_18700306_RT2");

    public static void main(String[] args) throws IOException, URISyntaxException {
        Main.main(specificBatch.getAbsolutePath());
    }
}
