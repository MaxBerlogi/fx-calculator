package com.anz;

public class FxRate {
	private Currency base;
	private Currency terms;
	private float rate;
	
	public FxRate(Currency base, Currency terms, float rate) {
		this.base = base;
		this.terms = terms;
		this.rate = rate;
	}
	
	public Currency getBase() { return base; }
	public Currency getTerms() { return terms; }
	public float getRate() { return rate; }
}
