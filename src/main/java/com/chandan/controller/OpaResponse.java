package com.chandan.controller;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonInclude(Include.NON_NULL)
public class OpaResponse {

	String regoText;

	List<String> results;
	
	public OpaResponse(List<String> results) {
		super();
		this.results = results;
	}

	public OpaResponse(String regoText) {
		this.regoText = regoText;
	}

	public String getRegoText() {
		return regoText;
	}

	public void setRegoText(String regoText) {
		this.regoText = regoText;
	}

	public List<String> getResults() {
		return results;
	}

	public void setResults(List<String> results) {
		this.results = results;
	}
	
}
