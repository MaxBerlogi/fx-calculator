package com.anz;

import java.util.HashMap;
import java.util.Map;

public enum Currency implements FxDetailed {
	AUD("Australian dollar", false),
	CAD("Canadian dollar", false),
	CNY("Chinese yuan", false),
	CZK("Czech koruna", true),
	DKK("Danish krone", true),	
	EUR("Euro", false),
	GBP("Pound sterling", false),
	JPY("Japanese yen", false),
	NOK("Norwegian krone", true),
	NZD("New Zealand dollar", false),
	USD("United States dollar", true);
	
	// Hash map is used to cache labels for easier by-label retrieval
    private static final Map<String, Currency> BY_LABEL = new HashMap<>();
     
    static {
        for (Currency c : values()) {
            BY_LABEL.put(c.label(), c);
        }
    }
 
    private final String label;
    private final boolean isEurCross;
 
    private Currency(String label, boolean isEurCross) {
        this.label = label;
        this.isEurCross = isEurCross;
    }
    
    // Get enum value by label
    public static Currency valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }
    
    @Override
    public String label() {
        return label;
    }
    
    @Override
    public boolean isEurCross() {
        return isEurCross;
    }
}
