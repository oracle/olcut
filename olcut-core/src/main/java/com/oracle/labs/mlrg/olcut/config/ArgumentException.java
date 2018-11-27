package com.oracle.labs.mlrg.olcut.config;

/** Indicates that a problem occurred while parsing arguments. */
public class ArgumentException extends RuntimeException {

    private final String argumentName;
    private final String otherArgumentName;
    protected final String msg;

    /**
     * Creates a new argument exception.
     *
     * @param argumentName The component this exception is related to.
     * @param msg          a description of the problem.
     */
    public ArgumentException(String argumentName, String msg) {
        this(null, argumentName, null, msg);
    }

    /**
     * Creates a new argument exception.
     *
     * @param argumentName The argument this exception is related to.
     * @param otherArgumentName The name of the conflicting argument.
     * @param msg          a description of the problem.
     */
    public ArgumentException(String argumentName, String otherArgumentName, String msg) {
        this(null, argumentName, otherArgumentName, msg);
    }


    /**
     * Creates a new argument exception.
     *
     * @param cause        The cause of exception. (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public ArgumentException(Throwable cause, String msg) {
        this(cause,null,null,msg);
    }

    /**
     * Creates a new argument exception.
     *
     * @param cause        The cause of the exception. (or <code>null</code> if unknown)
     * @param argumentName The argument this exception is related to.  (or <code>null</code> if unknown)
     * @param msg          a description of the problem.
     */
    public ArgumentException(Throwable cause, String argumentName, String msg) {
        this(cause,argumentName,null,msg);
    }

    /**
     * Creates a new argument exception.
     *
     * @param cause        The cause of the exception. (or <code>null</code> if unknown)
     * @param argumentName The argument this exception is related to.
     * @param otherArgumentName The name of the conflicting argument.
     * @param msg          a description of the problem.
     */
    public ArgumentException(Throwable cause, String argumentName, String otherArgumentName, String msg) {
        super(cause);

        this.argumentName = argumentName;
        this.otherArgumentName = otherArgumentName;
        this.msg = msg;
    }

    /** @return Returns the msg. */
    @Deprecated
    public String getMsg() {
        return msg;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if ((argumentName != null) && !argumentName.isEmpty()) {
            sb.append("Argument: ");
            sb.append(argumentName);
            sb.append(", ");
        }
        if ((otherArgumentName != null) && !otherArgumentName.isEmpty()) {
            sb.append("Other argument: ");
            sb.append(otherArgumentName);
            sb.append(", ");
        }
        sb.append(msg);
        return sb.toString();
    }

    /**
     * Retrieves the name of the offending argument
     *
     * @return the name of the offending argument
     */
    public String getArgument() {
        return otherArgumentName;
    }

    /**
     * Returns a string representation of this object
     *
     * @return the string representation of the object.
     */
    public String toString() {
        if (argumentName != null) {
            if (otherArgumentName != null) {
                return "Argument Exception argument:'" + argumentName + "' other argument:'" + otherArgumentName + "' - " + msg + '\n'
                        + super.toString();
            } else {
                return "Argument Exception argument:'" + argumentName + "' - " + msg + '\n' + super.toString();
            }
        } else {
            return "Argument Exception - " + msg + '\n' + super.toString();
        }

    }
}
