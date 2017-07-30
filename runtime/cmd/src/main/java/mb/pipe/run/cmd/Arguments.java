package mb.pipe.run.cmd;

import java.util.ArrayList;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = { "-D", "--drop" },
        description = "Drops cache and store before running pipeline") public boolean drop = false;

    @Parameter(names = { "-c", "--continuous" },
        description = "Listens for changes and continuously runs pipeline") public boolean continuous = false;

    @Parameter(names = { "-f", "--function" }, description = "Main pipeline function to run") public String function =
        "main";

    @Parameter(names = { "-a", "--arg" },
        description = "Pass argument to main pipeline function") public ArrayList<String> arguments = new ArrayList<>();


    @Parameter(names = { "-h", "--help" }, description = "Shows this usage information",
        help = true) public boolean help;

    @Parameter(names = { "--exit" }, description = "Immediately exit, used for testing purposes",
        hidden = true) public boolean exit;
}
