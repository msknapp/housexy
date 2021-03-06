package com.KnappTech.util;

public class DomainForInterval extends Domain {
	private double start = 0;
	private double end = 0;
	private boolean includeStart = false;
	private boolean includeEnd = false;
	
	public DomainForInterval(double start,boolean includeStart,
		double end,boolean includeEnd) {
		this.start = start;
		this.end = end;
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}
	
	@Override
	public boolean isInDomain(Double value) {
		if (value!=null && 
				value != Double.MAX_VALUE && 
				value!= Double.MIN_VALUE && 
				value!=  -Double.MAX_VALUE &&
				(value>start || (value==start && includeStart)) &&
				(value<end || (value==end && includeEnd))			)
		{
			return true;
		}
		return false;
	}
}
