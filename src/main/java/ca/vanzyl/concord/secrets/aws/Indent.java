package ca.vanzyl.concord.secrets.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class Indent {

    private int _level;
    private String _indent_string;
    private String _indentation = null;

    public Indent() {
        this(" ", 0);
    }

    public Indent(String indent_string) {
        this(indent_string, 0);
    }

    public Indent(int level) {
        this(" ", level);
    }

    public Indent(String indent_string, int level) {
        _indent_string = indent_string;
        _level = level;
        calculateIndentationString();
    }

    public String getIndentation() {
        return _indentation;
    }

    private void calculateIndentationString() {
        if (_level == 0) {
            _indentation = "";
            return;
        }

        // simple optim
        if (_level == 1) {
            _indentation = _indent_string;
            return;
        }

        StringBuilder sb = new StringBuilder(_level + _indent_string.length());

        for (int i = 0; i < _level; i++)
            sb.append(_indent_string);

        _indentation = sb.toString();
    }

    /**
     * Convenient method : returns the indentation depending on the level.
     *
     * @return the indentation
     */
    public String toString() {
        return getIndentation();
    }

    /**
     * Indents a block of text. You provide the block of text as a String and
     * the indent object and it returns another String with each line
     * properly indented.
     *
     * @param block  the block of text to indent
     * @param indent the indentation object to use
     * @return the indented block
     */
    public static String indentBlock(String block, int indent) {
        return indentBlock(block, new Indent(indent));
    }

    public static String indentBlock(String block, Indent indent) {
        StringBuilder sb = new StringBuilder();

        BufferedReader br = new BufferedReader(new StringReader(block));
        String line;
        try {
            while ((line = br.readLine()) != null)
                sb.append(indent).append(line).append('\n');
        } catch (IOException ex) {
            // on a String ? I doubt...
        }

        return sb.toString();
    }
}