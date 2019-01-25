package server.database;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author etienne
 */
public class DatabaseRequest {
    private AtomicBoolean completed;
    private AtomicBoolean errored;
    private Object result;
    private String error;
    private Object lock;
    private String type;
    
    public DatabaseRequest (String type){
        completed = new AtomicBoolean(false);
        errored = new AtomicBoolean(false);
        lock = new Object();
        this.type = type;
    }

    public void setCompleted(Boolean completed) {
        synchronized(lock) {
            this.completed.set(completed);
        }
    }

    public void setErrored(Boolean errored) {
        synchronized(lock) {
            this.errored.set(errored); 
        }
    }

    public void setResult(Object result) {
        synchronized(lock) {
            this.result = result;
        }
    }

    public void setError(String error) {
        synchronized(lock) {
            this.error = error;
        }
    }
    
    public boolean isCompleted () {
        return completed.get();
    }
    
    public boolean isErrored() {
        return errored.get();
    }
    
    public String getError() {
        if (errored.get()) {
            return error;
        }
        return null;
    }
    
    public Object getResult() {
        if (completed.get()) {
            return result;
        }
        return null;
    }

    public String getType() {
        return type;
    }
}
