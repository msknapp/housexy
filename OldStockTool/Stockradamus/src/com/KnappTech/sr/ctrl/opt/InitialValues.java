package com.KnappTech.sr.ctrl.opt;

import java.util.List;

import org.apache.commons.math.random.RandomVectorGenerator;

import com.KnappTech.sr.ctrl.PassiveUpdater;
import com.KnappTech.sr.model.comp.Company;
import com.KnappTech.sr.model.user.Investor;

public class InitialValues implements RandomVectorGenerator {

	private List<Company> companies = null;
	private double maxCostPerCompany = 0;
	private int timesCalled = 0;
	
	public InitialValues(List<Company> companies,Investor investor) {
		this.companies = companies;
		maxCostPerCompany = (investor.getDollarsAvailable())/(companies.size());
	}
	
	@Override
	public double[] nextVector() {
		double[] vector = new double[companies.size()];
		double price = 0;
		double maxShares = 0;
		for (int i = 0;i<companies.size();i++) {
			Company company = companies.get(i);
			if (company.getPriceHistory()==null || company.getLastKnownPrice()<=0) {
				PassiveUpdater.passivelyUpdatePriceHistory(company);
			}
			price = company.getLastKnownPrice();
			if (price>0) {
				maxShares = maxCostPerCompany/price;
				vector[i] = Math.random()*maxShares;
			} else {
				vector[i] = 0;
			}
		}
		timesCalled++;
		System.out.println("The initial values nextVector function has been called "+
				timesCalled+" times now.");
		return vector;
	}

}
