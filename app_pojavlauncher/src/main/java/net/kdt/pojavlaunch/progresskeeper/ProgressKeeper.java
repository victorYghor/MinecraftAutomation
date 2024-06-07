package net.kdt.pojavlaunch.progresskeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgressKeeper {
    private static final HashMap<String, List<ProgressListener>> sProgressListeners = new HashMap<>();
    /**
     * Isso daqui serve para linkar o nome do tipo de progress com o estado
     * do progresso, aqui guarda ele como um hashmap em que a chave é o nome
     * do tipo de progressso e o valor é o estado do progresso
     **/
    private static final HashMap<String, ProgressState> sProgressStates = new HashMap<>();
    private static final List<TaskCountListener> sTaskCountListeners = new ArrayList<>();

    /**
     * This is the mediator for the LayoutProgress Listener to update the progress
     * @param progressRecord
     * @param progress
     * @param message
     * @param va
     */
    public static synchronized void submitProgress(String progressRecord, int progress, String message, Object... va) {
        ProgressState progressState = sProgressStates.get(progressRecord);
        boolean shouldCallStarted = progressState == null;
        boolean shouldCallEnded = (progress == -1) || progress >= 100;
        if (shouldCallEnded) {
            shouldCallStarted = false;
            sProgressStates.remove(progressRecord);
            updateTaskCount();
        } else if (shouldCallStarted) {
            sProgressStates.put(progressRecord, (progressState = new ProgressState()));
            updateTaskCount();
        }
        if (progressState != null) {
            progressState.progress = progress;
            progressState.message = message;
            progressState.varArg = va;
        }

        // Apenas usando o listeners voce consegue dar um update no progresso da barra
        List<ProgressListener> progressListeners = sProgressListeners.get(progressRecord);
        if (progressListeners != null)
            for (ProgressListener listener : progressListeners) {
                if (shouldCallStarted) listener.onProgressStarted();
                else if (shouldCallEnded) listener.onProgressEnded();
                else listener.onProgressUpdated(progress, message, va);
            }
    }
    private static synchronized void updateTaskCount() {
        int count = sProgressStates.size();
        for (TaskCountListener listener : sTaskCountListeners) {
            listener.onUpdateTaskCount(count);
        }
    }

    public static synchronized void addListener(String progressRecord, ProgressListener listener) {
        ProgressState state = sProgressStates.get(progressRecord);
        if (state != null && (state.progress != -1)) {
            listener.onProgressStarted();
            listener.onProgressUpdated(state.progress, state.message, state.varArg);
        } else {
            listener.onProgressEnded();
        }
        List<ProgressListener> listenerWeakReferenceList = sProgressListeners.get(progressRecord);
        if (listenerWeakReferenceList == null)
            sProgressListeners.put(progressRecord, (listenerWeakReferenceList = new ArrayList<>()));
        listenerWeakReferenceList.add(listener);
    }

    public static synchronized void removeListener(String progressRecord, ProgressListener listener) {
        List<ProgressListener> listenerWeakReferenceList = sProgressListeners.get(progressRecord);
        if (listenerWeakReferenceList != null) listenerWeakReferenceList.remove(listener);
    }

    public static synchronized void addTaskCountListener(TaskCountListener listener) {
        listener.onUpdateTaskCount(sProgressStates.size());
        if (!sTaskCountListeners.contains(listener)) sTaskCountListeners.add(listener);
    }

    public static synchronized void addTaskCountListener(TaskCountListener listener, boolean runUpdate) {
        if (runUpdate) listener.onUpdateTaskCount(sProgressStates.size());
        if (!sTaskCountListeners.contains(listener)) sTaskCountListeners.add(listener);
    }

    public static synchronized void removeTaskCountListener(TaskCountListener listener) {
        sTaskCountListeners.remove(listener);
    }

    /**
     * Waits until all tasks are done and runs the runnable, or if there were no pending process remaining
     * The runnable runs from the thread that updated the task count last, and it might be the UI thread,
     * so don't put long running processes in it
     *
     * @param runnable the runnable to run when no tasks are remaining
     */
    public static void waitUntilDone(final Runnable runnable) {
        // If we do it the other way the listener would be removed before it was added, which will cause a listener object leak
        if (getTaskCount() == 0) {
            runnable.run();
            return;
        }
        TaskCountListener listener = new TaskCountListener() {
            @Override
            public void onUpdateTaskCount(int taskCount) {
                if (taskCount == 0) {
                    runnable.run();
                }
                removeTaskCountListener(this);
            }
        };
        addTaskCountListener(listener);
    }

    public static synchronized int getTaskCount() {
        return sProgressStates.size();
    }

    public static boolean hasOngoingTasks() {
        return getTaskCount() > 0;
    }
}
