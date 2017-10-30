package mb.spoofax.runtime.benchmark.state;

import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


@State(Scope.Benchmark)
public class IncrState {
    @Param({}) public String changedPathsStr;

    private List<PPath> changedTextFiles;
    private List<PPath> changedPaths;
    public List<PPath> allChangedPaths;


    public void setup(WorkspaceState workspaceState) throws IOException {
        this.changedTextFiles = new ArrayList<>();
        this.changedPaths = new ArrayList<>();
        this.allChangedPaths = new ArrayList<>();

        for(String changedPathStr : changedPathsStr.split("\\|")) {
            changedPathStr = changedPathStr.trim();
            if(changedPathStr.isEmpty()) continue;
            final boolean textFile = !changedPathStr.startsWith("@");
            final PPath changedPath = workspaceState.root.resolve(changedPathStr);
            if(textFile) {
                this.changedTextFiles.add(changedPath);
                modifyTextFile(changedPath);
            } else {
                this.changedPaths.add(changedPath);
                modifyPath(changedPath);
            }
            this.allChangedPaths.add(changedPath);
        }
    }

    public void renew() throws IOException {
        for(PPath changedTextFile : changedTextFiles) {
            modifyTextFile(changedTextFile);
        }
        for(PPath changedPath : changedPaths) {
            modifyPath(changedPath);
        }
    }


    private void modifyTextFile(PPath file) throws IOException {
        String text = new String(file.readAllBytes(), Charset.forName("UTF-8"));
        if(!text.endsWith(" ")) {
            text += " ";
        } else {
            text = text.trim();
        }
        try(final OutputStream outputStream = file.outputStream()) {
            outputStream.write(text.getBytes());
            outputStream.flush();
        }
    }

    private void modifyPath(PPath path) throws IOException {
        final boolean isDir = path.toString().endsWith("/") || path.toString().endsWith("\\");
        if(isDir) {
            path.touchDirectory();
        } else {
            path.touchFile();
        }
    }
}
