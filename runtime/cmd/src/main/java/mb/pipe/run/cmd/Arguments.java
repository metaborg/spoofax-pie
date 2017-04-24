package mb.pipe.run.cmd;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = { "--langspec" }, required = true,
        description = "Language specifification URI") public String langSpec;
    @Parameter(names = { "--config" }, required = true, description = "Configuration URI") public String config;
    @Parameter(names = { "--sdf" }, required = true, description = "SDF language URI") public String sdf;
    @Parameter(names = { "--sdf-spec" }, required = true, description = "SDF specification URI") public String sdfSpec;
    @Parameter(names = { "--esv" }, required = true, description = "ESV language URI") public String esv;
    @Parameter(names = { "--esv-spec" }, required = true, description = "ESV specification URI") public String esvSpec;
    @Parameter(names = { "--file" }, required = true, description = "File to parse URI") public String file;

    @Parameter(names = { "--clean" }, description = "Performs a clean build") public boolean clean;

    @Parameter(names = { "--continuous" },
        description = "Listens for changes and continuously builds") public boolean continuous;


    @Parameter(names = { "-h", "--help" }, description = "Shows this usage information",
        help = true) public boolean help;

    @Parameter(names = { "--exit" }, description = "Immediately exit, used for testing purposes",
        hidden = true) public boolean exit;
}
