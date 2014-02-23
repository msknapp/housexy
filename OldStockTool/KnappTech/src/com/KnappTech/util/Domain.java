package com.KnappTech.util;

public abstract class Domain {
	
	public Domain() {
		
	}
	
	public abstract boolean isInDomain(Double value);
	
	public static Domain greaterThanZeroDomain() {
		Domain d = new Domain() {
			@Override
			public boolean isInDomain(Double value) {
				if (value!=null && value>0 && 
						value != Double.MAX_VALUE && 
						value!= Double.MIN_VALUE && 
						value!= -Double.MAX_VALUE) {
					return true;
				}
				return false;
			}
		};
		return d;
	}
	
	public static Domain positiveDomain() {
		Domain d = new Domain() {
			@Override
			public boolean isInDomain(Double value) {
				if (value!=null && value>=0 && 
						value != Double.MAX_VALUE && 
						value!= Double.MIN_VALUE && 
						value!=  -Double.MAX_VALUE) {
					return true;
				}
				return false;
			}
		};
		return d;
	}
	
	public static Domain zeroToOneDomain() {
		Domain d = new Domain() {
			@Override
			public boolean isInDomain(Double value) {
				if (value!=null && 
						value>=0 && 
						value<=1 && 
						value != Double.MAX_VALUE && 
						value!= Double.MIN_VALUE && 
						value!=  -Double.MAX_VALUE)
				{
					return true;
				}
				return false;
			}
		};
		return d;
	}
	
	public static Domain zeroToOneHundredDomain() {
		Domain d = new Domain() {
			@Override
			public boolean isInDomain(Double value) {
				if (value!=null && value>=0 && value<=100 && 
						value != Double.MAX_VALUE && 
						value!= Double.MIN_VALUE) {
					return true;
				}
				return false;
			}
		};
		return d;
	}
	
	public static Domain realDomain() {
		Domain d = new Domain()
		{
			@Override
			public boolean isInDomain(Double value)
			{
				return (value!=null && 
					value != Double.MAX_VALUE && 
					value!= Double.MIN_VALUE && 
					value!=  -Double.MAX_VALUE);
			}
		};
		return d;
	}

	public static Domain negativeDomain() {
		Domain d = new Domain() {
			@Override
			public boolean isInDomain(Double value) {
				if (value!=null && value<=0 && 
						value!= -Double.MIN_VALUE && 
						value!=  -Double.MAX_VALUE) {
					return true;
				}
				return false;
			}
		};
		return d;
	}
}
