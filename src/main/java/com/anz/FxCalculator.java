package com.anz;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import org.json.JSONException;

public class FxCalculator {
	
	private final static Logger logger = Logger.getLogger("FxCalculatorLog");
	
	public static void main(String[] args) {
		// Provide program infrastructure.
		Properties properties = new Properties();
		int permits;
		FxConverter converter;
		try {
			setUpLogging();
			// Load properties.
			properties.load(FxCalculator.class.getClassLoader().getResourceAsStream("app.properties"));
			// Get permits for concurrent conversions.
			permits = Integer.parseInt(properties.getProperty("converterPermits"));
			// Converter does currency conversions asynchronously.
			converter = new FxConverter(permits);
		} catch (IOException|NumberFormatException ex) {
			System.err.println("Cannot load configuration files.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			return;
		} catch (JSONException|IllegalArgumentException ex) {
			System.err.println("Cannot parse direct feed rates from file.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			return;
		} catch (Exception ex) {
			System.err.println("Cannot initialize FxConverter.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			return;
		}

		// Give instructions.
		System.out.println("Type in a line the form of \"<convert from> <number of units> in <convert to>\" or \"quit\" to exit.");
		// Read input until user quits, then dispose of the scanner.
		try (Scanner scanner = new Scanner(System.in)) {
			String input;
			ExecutorService executor = Executors.newCachedThreadPool();
			while ((input = scanner.nextLine()) != null && !input.equals("quit")) {
				// Do calculations asynchronously in order not to block the UI thread.
				final String finalInput = input;
				executor.submit(new Runnable() {
					public void run() {
						converter.tryConvert(finalInput);
					}
				});
			}
			// Force shutdown when quit was called.
			executor.shutdownNow();
		}
	}
	
	private static void setUpLogging() {
		// Reset default logger.
		LogManager.getLogManager().reset();
		// In production do not bother user with cumbersome stack prints.
		logger.setLevel(Level.SEVERE);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.SEVERE);
		logger.addHandler(ch);
	}
}
