package at.salzburgresearch.nodekeeper.bootstrap;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * ZooKeeper Bootstrap Command-Line Interface
 *
 * @author Sergio Fernández
 */
public class ZKBootstrapCli {

    private File file;
    private String connection;

    public static void main(String[] args) throws ParseException, InterruptedException, NodeKeeperException, IOException {
        new ZKBootstrapCli(args).exec();
    }

    public ZKBootstrapCli(String[] args) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(buildOptions(), args);

        if(line.hasOption("help")) {
            displayUsage();
        } else {
            if(line.hasOption("file")) {
               String path = line.getOptionValue("file");
               file = new File(path);
                if (!file.exists() || !file.canRead()) {
                    System.err.println("Bootstrap file '" + file.getAbsolutePath() + " not found!");
                    displayUsage();
                }
                if (line.hasOption("connection")) {
                    connection = line.getOptionValue("connection");
                } else {
                    connection = "localhost:2181";
                }
            } else {
                System.err.println("Bootstrap file missing!");
                displayUsage();
            }
        }
    }

    public void exec() throws InterruptedException, IOException, NodeKeeperException {
        NodeKeeper nodeKeeper = new NodeKeeper(connection, 10, new Properties());
        ZKBootstrap zkBootstrap = new ZKBootstrap(nodeKeeper);
        zkBootstrap.load(file, true);
        System.out.println("Successfully bootstrapped zookeeper at " + connection + " with data from " + file.getAbsolutePath() + " !");
    }

    private Options buildOptions() {
        Option help = OptionBuilder.withDescription("display help message")
                                        .withLongOpt("help")
                                        .create("h");

        Option file = OptionBuilder.withArgName("file")
                                        .hasArg()
                                        .isRequired()
                                        .withDescription("path to bootstrap properties file")
                                        .withLongOpt("file")
                                        .create("f");

        Option connection = OptionBuilder.withArgName("connection")
                                        .hasArg()
                                        .isRequired(false)
                                        .withDescription("connection string to zookeeper (default localhost:2181)")
                                        .withLongOpt("connection")
                                        .create("c");


        Options options = new Options();
        options.addOption(help);
        options.addOption(file);
        options.addOption(connection);
        return options;
    }

    private void displayUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar nodekeeper-java-1.1-onejar.jar", buildOptions() );
        System.exit(-1);
    }

}
