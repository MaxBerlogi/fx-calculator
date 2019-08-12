package com.anz;

/** Interface specifies basic details about the currency
 * @label Name of the currency
 * @isEurCross Indicates whether currency is crossed via EUR
*/
public interface FxDetailed {
	String label();
	boolean isEurCross();
}
