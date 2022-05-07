package dev.coldhands.pair.stairs.cli;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

record StdIO(StringWriter out,
             PrintWriter outWriter,
             StringWriter err,
             PrintWriter errWriter,
             InputStream in,
             OutputStreamWriter inputWriter) {
}
