package core;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class TraceFileReader implements Iterable<TraceEvent>{
    private final Queue<TraceFile> traceFileList;
    private TraceFile traceFile;
    private final Iterator<TraceEvent> itr;

    public TraceFileReader (Queue<TraceFile> traceFileList) {
        this.traceFileList = traceFileList;
        traceFile = traceFileList.poll();
        itr = traceFile.iterator();
    }

    private boolean nextFile() {
        traceFile = traceFileList.poll();
        return (traceFile != null);
    }

    @Override
    public Iterator<TraceEvent> iterator() {
        return new Iterator<TraceEvent>() {
            @Override
            public boolean hasNext() {
                if (traceFile.hasEvent()) {
                    return true;
                }
                return nextFile();
            }

            @Override
            public TraceEvent next() {
                if (!itr.hasNext()) {
                    if (!nextFile()) {
                        return null;
                    }
                }
                return itr.next();
            }
        };
    }
}
