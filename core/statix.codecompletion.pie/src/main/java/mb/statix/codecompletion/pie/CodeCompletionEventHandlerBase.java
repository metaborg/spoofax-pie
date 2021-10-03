package mb.statix.codecompletion.pie;

public /* open */ class CodeCompletionEventHandlerBase implements CodeCompletionEventHandler {
    private State state = State.Ready;

    @Override
    public void begin() {
        if(this.state != State.Ready) throw new IllegalStateException("Not ready.");
        this.state = State.Running;
    }

    @Override
    public void beginParse() {
        if(this.state != State.Running) throw new IllegalStateException("Not running.");
        this.state = State.Parsing;
    }

    @Override
    public void endParse() {
        if(this.state != State.Parsing) throw new IllegalStateException("Not parsing.");
        this.state = State.Running;
    }

    @Override
    public void beginPreparation() {
        if(this.state != State.Running) throw new IllegalStateException("Not running.");
        this.state = State.Preparing;
    }

    @Override
    public void endPreparation() {
        if(this.state != State.Preparing) throw new IllegalStateException("Not preparing.");
        this.state = State.Running;
    }

    @Override
    public void beginAnalysis() {
        if(this.state != State.Running) throw new IllegalStateException("Not running.");
        this.state = State.Analyzing;
    }

    @Override
    public void endAnalysis() {
        if(this.state != State.Analyzing) throw new IllegalStateException("Not analyzing.");
        this.state = State.Running;
    }

    @Override
    public void beginCodeCompletion() {
        if(this.state != State.Running) throw new IllegalStateException("Not running.");
        this.state = State.Completing;
    }

    @Override
    public void endCodeCompletion() {
        if(this.state != State.Completing) throw new IllegalStateException("Not completing.");
        this.state = State.Running;
    }

    @Override
    public void beginFinishing() {
        if(this.state != State.Running) throw new IllegalStateException("Not running.");
        this.state = State.Finishing;
    }

    @Override
    public void endFinishing() {
        if(this.state != State.Finishing) throw new IllegalStateException("Not finishing.");
        this.state = State.Running;
    }

    @Override
    public void end() {
        if(this.state != State.Running) throw new IllegalStateException("Not running.");
        this.state = State.Done;
    }

    private enum State {
        Ready,
        Preparing,
        Running,
        Parsing,
        Analyzing,
        Completing,
        Finishing,
        Done;
    }

}
