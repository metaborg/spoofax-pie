package mb.statix.codecompletion.pie;

public /* open */ class CodeCompletionEventHandlerBase implements CodeCompletionEventHandler {
    private State state = State.Ready;

    @Override
    public void begin() {
        if(this.state != State.Ready) throw new IllegalStateException("Not ready, but " + state);
        this.state = State.Running;
    }

    @Override
    public void beginParse() {
        if(this.state != State.Running) throw new IllegalStateException("Not running, but " + state);
        this.state = State.Parsing;
    }

    @Override
    public void endParse() {
        if(this.state != State.Parsing) throw new IllegalStateException("Not parsing, but " + state);
        this.state = State.Running;
    }

    @Override
    public void beginPreparation() {
        if(this.state != State.Running) throw new IllegalStateException("Not running, but " + state);
        this.state = State.Preparing;
    }

    @Override
    public void endPreparation() {
        if(this.state != State.Preparing) throw new IllegalStateException("Not preparing, but " + state);
        this.state = State.Running;
    }

    @Override
    public void beginAnalysis() {
        if(this.state != State.Running) throw new IllegalStateException("Not running, but " + state);
        this.state = State.Analyzing;
    }

    @Override
    public void endAnalysis() {
        if(this.state != State.Analyzing) throw new IllegalStateException("Not analyzing, but " + state);
        this.state = State.Running;
    }

    @Override
    public void beginCodeCompletion() {
        if(this.state != State.Running) throw new IllegalStateException("Not running, but " + state);
        this.state = State.Completing;
    }

    @Override
    public void endCodeCompletion() {
        if(this.state != State.Completing) throw new IllegalStateException("Not completing, but " + state);
        this.state = State.Running;
    }

    @Override
    public void beginFinishing() {
        if(this.state != State.Running) throw new IllegalStateException("Not running, but " + state);
        this.state = State.Finishing;
    }

    @Override
    public void endFinishing() {
        if(this.state != State.Finishing) throw new IllegalStateException("Not finishing, but " + state);
        this.state = State.Running;
    }

    @Override
    public void end() {
        if(this.state != State.Running) throw new IllegalStateException("Not running, but " + state);
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
