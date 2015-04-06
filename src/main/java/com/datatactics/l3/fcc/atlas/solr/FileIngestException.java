package com.datatactics.l3.fcc.atlas.solr;

import java.io.File;

public class FileIngestException extends Exception {
    private static final long serialVersionUID = 1L;

    private String filePath;
    
    public FileIngestException(Throwable t, String filePath) {
        super("error ingesting file at " + filePath, t);

        this.filePath = filePath;
    }

    public FileIngestException(File file, Throwable t) {
        this(t, file.getAbsolutePath());
    }
    
    public FileIngestException(Throwable t) {
        super("error during file ingest", t);
    }
    
    public boolean hasFilePath() {
        return (filePath != null && !filePath.trim().equals(""));
    }
    
    public String getFilePath() {
        return filePath;
    }
}
