package org.apache.tika.langdetect;

import java.util.Locale;

public class LanguageResult {

	// A result that indicates no match. Used when no language was detected.
	public static final LanguageResult NULL = new LanguageResult("", LanguageConfidence.NONE, 0.0f);
	
	private String language;
	
	private LanguageConfidence confidence;
	
	// rawScore should be a number from 0.0 to 1.0, with higher values implying
	// greater confidence.
	private float rawScore;
	
	/**
	 * 
	 * @param language ISO 639-1 language code (plus optional "-<country code>")
	 * @param rawScore confidence of detector in the result.
	 */
	public LanguageResult(String language, LanguageConfidence confidence, float rawScore) {
		this.language = language;
		this.confidence = confidence;
		this.rawScore = rawScore;
	}

	public String getLanguage() {
		return language;
	}

	public float getRawScore() {
		return rawScore;
	}
	
	public LanguageConfidence getConfidence() {
		return confidence;
	}
	
	public boolean isReasonablyCertain() {
		return confidence == LanguageConfidence.HIGH;
	}
	
	public boolean isUnknown() {
		return confidence == LanguageConfidence.NONE;
	}
	
	/**
	 * Return true if the target language matches the detected language. We consider
	 * it a match if, for the precision requested or detected, it matches. This means:
	 * 
	 * target	|	detected	| match?
	 * zh		|	en			| false
	 * zh		|	zh			| true
	 * zh		|	zh-CN		| true
	 * zh-CN	|	zh			| true
	 * zh-CN	|	zh-TW		| false
	 * zh-CN	|	zh-cn		| true (case-insensitive)
	 * 
	 * @param language
	 * @return
	 */
	public boolean isLanguage(String language) {
		String[] targetLanguage = language.split("\\-");
		String[] resultLanguage = this.language.split("\\-");
		
		int minLength = Math.min(targetLanguage.length, resultLanguage.length);
		for (int i = 0; i < minLength; i++) {
			if (!targetLanguage[i].equalsIgnoreCase(resultLanguage[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.US, "%s: %s (%f)", language, confidence, rawScore);
	}
}
