package mb.pipe.run.cmd;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = { "--clean" }, description = "Performs a clean build") public boolean clean;

    @Parameter(names = { "--continuous" },
        description = "Listens for changes and continuously builds") public boolean continuous;


    @Parameter(names = { "-h", "--help" }, description = "Shows this usage information",
        help = true) public boolean help;

    @Parameter(names = { "--exit" }, description = "Immediately exit, used for testing purposes",
        hidden = true) public boolean exit;
}
