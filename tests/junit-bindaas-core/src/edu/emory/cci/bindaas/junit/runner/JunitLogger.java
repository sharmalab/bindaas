package edu.emory.cci.bindaas.junit.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Locale;


public class JunitLogger extends PrintStream{

	private PrintStream console;
	public JunitLogger(File file) throws FileNotFoundException {
		super(file);
		console = System.out;
	}
	@Override
	public PrintStream append(char c) {
		// TODO Auto-generated method stub
		return super.append(c);
	}
	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		// TODO Auto-generated method stub
		return super.append(csq, start, end);
	}
	@Override
	public PrintStream append(CharSequence csq) {
		// TODO Auto-generated method stub
		return super.append(csq);
	}
	@Override
	public boolean checkError() {
		// TODO Auto-generated method stub
		return super.checkError();
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
		console.close();
	}
	@Override
	public void flush() {
		// TODO Auto-generated method stub
		super.flush();
		console.flush();
	}
	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		// TODO Auto-generated method stub
		return super.format(l, format, args);
	}
	@Override
	public PrintStream format(String format, Object... args) {
		// TODO Auto-generated method stub
		return super.format(format, args);
	}
	@Override
	public void print(boolean b) {
		// TODO Auto-generated method stub
		super.print(b);
		console.print(b);
	}
	@Override
	public void print(char c) {
		// TODO Auto-generated method stub
		super.print(c);
		console.print(c);
	}
	@Override
	public void print(char[] s) {
		// TODO Auto-generated method stub
		super.print(s);
		console.print(s);
	}
	@Override
	public void print(double d) {
		// TODO Auto-generated method stub
		super.print(d);
		console.print(d);
	}
	@Override
	public void print(float f) {
		// TODO Auto-generated method stub
		super.print(f);
		console.print(f);
	}
	@Override
	public void print(int i) {
		// TODO Auto-generated method stub
		super.print(i);
		console.print(i);
	}
	@Override
	public void print(long l) {
		// TODO Auto-generated method stub
		super.print(l);
		console.print(l);
	}
	@Override
	public void print(Object obj) {
		// TODO Auto-generated method stub
		super.print(obj);
		console.print(obj);
	}
	@Override
	public void print(String s) {
		// TODO Auto-generated method stub
		super.print(s);
		console.print(s);
	}
	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		// TODO Auto-generated method stub
		return super.printf(l, format, args);
		
	}
	@Override
	public PrintStream printf(String format, Object... args) {
		// TODO Auto-generated method stub
		return super.printf(format, args);
	}
	@Override
	public void println() {
		// TODO Auto-generated method stub
		super.println();
		console.println();
	}
	@Override
	public void println(boolean x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(char x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(char[] x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(double x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(float x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(int x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(long x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(Object x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}
	@Override
	public void println(String x) {
		// TODO Auto-generated method stub
		super.println(x);
		console.println(x);
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		// TODO Auto-generated method stub
		super.write(buf, off, len);
		console.write(buf, off, len);
	}
	@Override
	public void write(int b) {
		// TODO Auto-generated method stub
		super.write(b);
		console.write(b);
	}

	
}
