package de.keybird.build.dbupdate;

public class UpdateNotPossibleException extends Exception {

    private static final long serialVersionUID = 1L;

    public UpdateNotPossibleException() {

    }

    public UpdateNotPossibleException(String msg, Throwable e) {
        super(msg, e);
    }

    public UpdateNotPossibleException(String msg) {
        super(msg);
    }

}
