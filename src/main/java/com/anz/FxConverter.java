package com.anz;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

import org.json.*;

/**
 * Class contains a list of rates for currency pairs and is able to make several concurrent calculations to convert units of one currency into another.
 * @author Max Alexson
 */
public class FxConverter {
	
	Logger logger = Logger.getLogger("FxCalculatorLog");
	
	private ArrayList<FxRate> rates = new ArrayList<FxRate>();
	
	private Semaphore semaphore;
	
	/**
	 * Initialize converter with rates and a semaphore to control max concurrent threads.
	 * @throws JSONException 
	 * @throws FileNotFoundException 
	 */
	public FxConverter(int permits) 
			throws FileNotFoundException, JSONException {
		loadRates();
		semaphore = new Semaphore(permits, true);
	}
	
	/**
	 * Fill in the rates for all possible combinations of given currencies when FxConverter is created.
	 * @throws JSONException 
	 * @throws FileNotFoundException 
	 */
	private void loadRates() 
			throws FileNotFoundException, JSONException {
		loadDirectFeedRates();
		addInvertedRates();
		addCrossRates();
	}
	
	/**
	 * Loads rates known initially from a file.
	 * @throws JSONException 
	 * @throws FileNotFoundException 
	 */
	private void loadDirectFeedRates() 
			throws FileNotFoundException, JSONException {
		JSONTokener tokener = new JSONTokener(this.getClass().getClassLoader().getResourceAsStream("directFeeds.json"));
		JSONArray directFeeds = new JSONArray(tokener);
		for (int i=0; i < directFeeds.length(); i++) {
			String base = directFeeds.getJSONObject(i).getString("base");
			String terms = directFeeds.getJSONObject(i).getString("terms");
			float rate = (float)directFeeds.getJSONObject(i).getDouble("rate");
			rates.add(new FxRate(Currency.valueOf(base), Currency.valueOf(terms), rate));
		}
	}
	
	/**
	 * Calculates inverted rates from direct feeds and caches them in the rates list.
	 */
	private void addInvertedRates() {
		// Use array since the size of direct feeds is known.
		FxRate[] invertedRates = new FxRate[10];
		for (int i = 0; i < invertedRates.length; i++) {
			// Swap base and terms currencies within direct feed and divide 1 by the rate value.
			invertedRates[i] = new FxRate(rates.get(i).getTerms(), rates.get(i).getBase(), 1/rates.get(i).getRate());
		}
		// Add all inverted rates to the rates collection.
		rates.addAll(Arrays.asList(invertedRates));
	}
	
	/**
	 * Calculates rates for all currency pairs by crossing them via EUR or USD and caches them in the rates list. 
	 */
	private void addCrossRates() {
		Currency[] currencies = Currency.values();
		// Go through all possible combinations of currencies.
		for(int i=0; i<currencies.length; i++) {
	        for(int j=0; j<currencies.length; j++) {
	        	addRate(currencies[i], currencies[j]);
	        }
		}
	}
	
	/**
	 * Adds a rate to rates list if it has not been added yet.
	 * @param base
	 * @param terms
	 */
	private void addRate(Currency base, Currency terms) {
		// Put this quick check first since 
		// 1 to 1 rates have not been added yet.
		if (base == terms) {
			rates.add(new FxRate(base, terms, 1f));
			return;
		}
		// Return if the rate has already been added.
		if (rates.stream().anyMatch(rate -> rate.getBase() == base && rate.getTerms() == terms))
			return;
		// All checks passed this is a cross. Determine which currency to cross it via.
		// For EUR crosses both base and terms must be EurCross.
		Currency crossVia = (base.isEurCross() && terms.isEurCross()) ? Currency.EUR : Currency.USD;
		addCrossRate(base, terms, crossVia);
	}
	
	/**
	 * Calculates cross rate and adds it to the rates list.
	 * @param base
	 * @param terms
	 * @param crossVia
	 */
	private void addCrossRate(Currency base, Currency terms, Currency crossVia) {
		// Get the base/crossVia rate.
		FxRate baseCrossVia;
		try {
			baseCrossVia = rates.stream().filter((rate) -> rate.getBase() == base && rate.getTerms() == crossVia).findFirst().orElseThrow(IllegalStateException::new);
		} catch (IllegalStateException ex) {
			// Terms is missing. Find it by crossing via another currency.
			Currency knownRateCurrency = (crossVia == Currency.EUR) ? Currency.USD : Currency.EUR;
			// It is guaranteed that crossing via knownRateCurrency will not throw, 
			// since rates that are not known for USD are known for EUR and vice versa.
			// Add rate with recursion, without fear of infinite loop.
			addCrossRate(base, crossVia, knownRateCurrency);
			// Repeat the operation after terms was added.
			baseCrossVia = rates.stream().filter((rate) -> rate.getBase() == base && rate.getTerms() == crossVia).findFirst().orElseThrow(IllegalStateException::new);
		}
		// Get the crossVia/terms rate.
		FxRate crossViaTerms;
		try {
			crossViaTerms = rates.stream().filter((rate) -> rate.getBase() == crossVia && rate.getTerms() == terms).findFirst().orElseThrow(IllegalStateException::new);
		} catch (IllegalStateException ex) {
			// Base is missing. Find it by crossing via another currency.
			Currency knownRateCurrency = (crossVia == Currency.EUR) ? Currency.USD : Currency.EUR;
			// Add rate recursively.
			addCrossRate(crossVia, terms, knownRateCurrency);
			// Repeat the operation after base was added.
			crossViaTerms = rates.stream().filter((rate) -> rate.getBase() == crossVia && rate.getTerms() == terms).findFirst().orElseThrow(IllegalStateException::new);
		}
		// Add the newly calculated rate.
		rates.add(new FxRate(base, terms, baseCrossVia.getRate()*crossViaTerms.getRate()));
	}
	
	/**
	 * Processes user input and performs currency conversion.
	 * @param input
	 */
	public void tryConvert(String input) {
		try {
			semaphore.acquire();
			// Validate input is in correct format.
			if (!input.matches("^([A-Z]{3} \\d+(\\.\\d{2})? in [A-Z]{3})$"))
				throw new InputMismatchException("Invalid user input: " + input);
			// Parse currencies.
			String[] inputValues = input.split(" ");
			// Extra try catch to format the warning as per project requirements.
			// Can be caught in external catch like with other exceptions if not
			// for the specific error message.
			FxRate fxRate;
			float units;
			try {
				Currency base = Currency.valueOf(inputValues[0]);
				Currency terms = Currency.valueOf(inputValues[3]);
				// Parse units:
				units = Float.parseFloat(inputValues[1]);
				// Find rate:
				fxRate = rates.stream().filter((rate) -> rate.getBase() == base && rate.getTerms() == terms).findFirst().orElseThrow(IllegalStateException::new);
			} catch (IllegalArgumentException|IllegalStateException ex) {
				System.err.println("Unable to find rate for " + inputValues[0] + "/" + inputValues[3]);
				logger.log(Level.WARNING, ex.getMessage(), ex);
				return;
			}
			// Calculate and round the result.
			if (fxRate.getTerms() == Currency.JPY) {
				// Float is guaranteed to round to integer without casting.
				int result = Math.round(units * fxRate.getRate());
				System.out.println("" + fxRate.getBase() + " " + inputValues[1] + " = " + fxRate.getTerms() + " " + result);
			} else {
				float result = Math.round((units * fxRate.getRate())*100f)/100f;
				System.out.println("" + fxRate.getBase() + " " + inputValues[1] + " = " + fxRate.getTerms() + " " + result);
			}

		} catch (InterruptedException ex) {
			System.err.println("Conversion interrupted.");
			logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (InputMismatchException ex) {
			System.err.println("Incorrect input. Type in a line the form of \"<convert from> <number of units> in <convert to>\". Example: AUD 100 in USD.");
			logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (Exception ex) {
			System.err.println("Unexpected error.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		finally {
			semaphore.release();
		}
	}
}
