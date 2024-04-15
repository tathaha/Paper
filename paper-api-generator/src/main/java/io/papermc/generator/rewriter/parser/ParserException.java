package io.papermc.generator.rewriter.parser;

public class ParserException extends IllegalStateException {

    private final StringReader reader;

    public ParserException(String message, StringReader reader) {
        super(message);
        this.reader = reader;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        message.append(" near/at position ").append(this.reader.getCursor());
        if (this.reader.canRead()) {
            message.append(": ").append(this.reader.peek());
        }
        message.append(" (in line: ").append('"').append(this.reader.getString()).append('"').append(')');

        return message.toString();
    }
}
