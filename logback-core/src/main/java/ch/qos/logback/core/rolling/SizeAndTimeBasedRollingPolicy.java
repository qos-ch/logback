package ch.qos.logback.core.rolling;



public class SizeAndTimeBasedRollingPolicy<E> extends TimeBasedRollingPolicy<E> {

    String maxFileSizeAsString;
    
    @Override
    public void start() {
        SizeAndTimeBasedFNATP<E> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<E>(); 
        if(maxFileSizeAsString == null) {
            addError("MaxFileSize property must be set");
            return;
        } else {
            addInfo("Achive files will be limied to ["+maxFileSizeAsString+"] each.");
        }
        
        sizeAndTimeBasedFNATP.setMaxFileSize(maxFileSizeAsString);
        timeBasedFileNamingAndTriggeringPolicy = sizeAndTimeBasedFNATP;
        
        // most work is done by the parent
        super.start();
    }
    
    
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSizeAsString = maxFileSize;
    }
    
    @Override
    public String toString() {
        return "c.q.l.core.rolling.SizeAndTimeBasedRollingPolicy@"+this.hashCode();
    }
}
