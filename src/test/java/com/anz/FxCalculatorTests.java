package com.anz;

import static org.junit.Assert.*;

import java.io.*;

import org.json.JSONException;
import org.junit.*;

/**
 * Test examines out and error streams after calling convert method of FxConverter.
 * @author Max Alexson
 *
 */
public class FxCalculatorTests {
	private final ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
	private final ByteArrayOutputStream sysErr = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;
	
	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(sysOut));
		System.setErr(new PrintStream(sysErr));;
	}
	
	@After
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}
	
	@Test
	public void testFxCalculation() {
		FxConverter converter;
		try {
			converter = new FxConverter(1);
		} catch (FileNotFoundException e) {
			fail();
			return;
		} catch (JSONException e) {
			fail();
			return;
		}
		converter.tryConvert("NOK 100.34 in JPY");
		// %n is needed since actual output has a carriage return at the end:
		assertEquals(String.format("NOK 100.34 = JPY 1711%n"), sysOut.toString());
	}
	
	@Test
	public void testInvalidInput() {
		FxConverter converter;
		try {
			converter = new FxConverter(1);
		} catch (FileNotFoundException e) {
			fail();
			return;
		} catch (JSONException e) {
			fail();
			return;
		}
		converter.tryConvert("some invalid input");
		assertEquals(String.format("Incorrect input. Type in a line the form of \"<convert from> <number of units> in <convert to>\". Example: AUD 100 in USD.%n"), sysErr.toString());
	}
	
	@Test
	public void testUnknownRate() {
		FxConverter converter;
		try {
			converter = new FxConverter(1);
		} catch (FileNotFoundException e) {
			fail();
			return;
		} catch (JSONException e) {
			fail();
			return;
		}
		converter.tryConvert("ABC 100.20 in XYZ");
		String[] messageLines = sysErr.toString().split("\\R", 2);
		assertEquals("Unable to find rate for ABC/XYZ", messageLines[0]);
	}
}
