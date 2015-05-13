package cn.uncode.dal.exception;

public class StaleObjectStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public StaleObjectStateException() {
        super();
    }

    public StaleObjectStateException(String message) {
        super(message);
    }


    public StaleObjectStateException(String message, Throwable cause) {
        super(message, cause);
    }


    public StaleObjectStateException(Throwable cause) {
        super(cause);
    }


    protected StaleObjectStateException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
