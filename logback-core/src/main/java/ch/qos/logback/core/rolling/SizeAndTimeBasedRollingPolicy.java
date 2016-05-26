package ch.qos.logback.core.rolling;

import ch.qos.logback.core.util.FileSize;



public class SizeAndTimeBasedRollingPolicy<E> extends TimeBasedRollingPolicy<E> {

    FileSize maxFileSize;
    
    @Override
    public void start() {
        SizeAndTimeBasedFNATP<E> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<E>(); 
        if(maxFileSize == null) {
            addError("MaxFileSize property must be set");
            return;
        } else {
            addInfo("Achive files will be limied to ["+maxFileSize+"] each.");
        }
        
        sizeAndTimeBasedFNATP.setMaxFileSize(maxFileSize);
        timeBasedFileNamingAndTriggeringPolicy = sizeAndTimeBasedFNATP;
        
        // most work is done by the parent
        super.start();
    }
    
    
    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }
    
    @Override
    public String toString() {
        return "c.q.l.core.rolling.SizeAndTimeBasedRollingPolicy@"+this.hashCode();
    }
}
