package miraj.biid.com.pani_200;

public abstract class ProgressMonitor {
    protected int currentProgress = 0;
    private int currentProgressSubJob;
    private int stepsSubJob;
    private int startSubJob;
    private int creditsSubJob;

    protected abstract void setProgress(int progress);
    protected abstract void setMessage(String message);

    protected void worked(int steps) {
        if (steps != 0) {
            setProgress(currentProgress + steps);
        }
    };

    public void initSubJob(int stepsSubJob, int creditsSubJob) {
        this.currentProgressSubJob = 0;
        this.startSubJob = currentProgress;
        this.creditsSubJob = creditsSubJob;
        this.stepsSubJob = stepsSubJob;

    }
    public void workedSubJob() {
        workedSubJob(currentProgressSubJob + 1);
    }

    public void endSubJob() {
        setProgress(startSubJob + creditsSubJob);
    }

    public void workedSubJob(int progress) {
        currentProgressSubJob = progress;
        setProgress(startSubJob + (int)(Math.ceil((double)creditsSubJob * (double)currentProgressSubJob / (double)stepsSubJob)));
    }
}
