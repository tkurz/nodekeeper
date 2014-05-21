package at.salzburgresearch.nodekeeper.bootstrap;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
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

    private Boolean type_is_import = true;
    private String root = "/";

    private boolean clean = false;

    public static void main(String[] args) throws ParseException, InterruptedException, NodeKeeperException, IOException {
        new ZKBootstrapCli(args).exec();
    }

    public ZKBootstrapCli(String[] args) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            line = parser.parse(buildOptions(), args);
        } catch (MissingOptionException e) {
            System.err.println(e.getMessage());
            displayUsage();
        }

        if(line.hasOption("help")) {
            displayUsage();
        } else {
            if(line.hasOption("file")) {
               String path = line.getOptionValue("file");

               if(line.hasOption("type"))  {
                   if(line.getOptionValue("type").equals("export")) {
                       type_is_import = false;
                   };
               }

               file = new File(path);

                if(type_is_import) {
                    if(line.hasOption("clean"))  {
                        clean = Boolean.parseBoolean(line.getOptionValue("clean"));
                    }
                    if (!file.exists() || !file.canRead()) {
                        System.err.println("Bootstrap file '" + file.getAbsolutePath() + " not found!");
                        displayUsage();
                    }
                } else {
                    if(line.hasOption("root"))  {
                        root = line.getOptionValue("root");
                    }
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
        NodeKeeper nodeKeeper = new NodeKeeper(connection, 10000, new Properties());
        ZKBootstrap zkBootstrap = new ZKBootstrap(nodeKeeper);
        if(type_is_import) {
            zkBootstrap.load(file, clean);
            System.out.println("Successfully bootstrapped zookeeper at " + connection + " with data from " + file.getAbsolutePath() + " !");
        } else {
            FileOutputStream out = new FileOutputStream(file);
            zkBootstrap.write(out,root);
            out.flush();
            out.close();
            System.out.println("Successfully stored zookeeper at " + connection + " in " + file.getAbsolutePath() + " !");
        }

    }

    private Options buildOptions() {
        Option help = OptionBuilder.withDescription("display help message")
                                        .withLongOpt("help")
                                        .create("h");

        Option file = OptionBuilder.withArgName("file")
                                        .hasArg()
                                        .isRequired()
                                        .withDescription("path to file")
                                        .withLongOpt("file")
                                        .create("f");

        Option type = OptionBuilder.withArgName("type")
                .hasArg()
                .isRequired(false)
                .withDescription("type import or export (default is import)")
                .withLongOpt("type")
                .create("t");

        Option connection = OptionBuilder.withArgName("connection")
                                        .hasArg()
                                        .isRequired(false)
                                        .withDescription("connection string to zookeeper (default localhost:2181)")
                                        .withLongOpt("connection")
                                        .create("c");

        Option root = OptionBuilder.withArgName("root")
                .hasArg()
                .isRequired(false)
                .withDescription("root-node for export, default is /")
                .withLongOpt("root")
                .create("r");

        Option clean = OptionBuilder.withArgName("clean")
                .hasArg()
                .isRequired(false)
                .withDescription("if all nodes should be deleted before importing (default is false)")
                .withLongOpt("clean")
                .create("e");


        Options options = new Options();
        options.addOption(help);
        options.addOption(file);
        options.addOption(connection);
        options.addOption(type);
        options.addOption(root);
        options.addOption(clean);
        return options;
    }

    private void displayUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar nodekeeper-java-1.2-onejar.jar", buildOptions() );
        System.exit(-1);
    }

}
