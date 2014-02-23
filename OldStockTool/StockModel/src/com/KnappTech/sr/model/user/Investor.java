package com.KnappTech.sr.model.user;

import java.util.Collection;
import java.util.HashSet;

import com.KnappTech.model.KTObject;
import com.KnappTech.model.ReportableSet;
//import com.KnappTech.sr.model.PropertyManager;
//import com.KnappTech.sr.model.comp.Companies;
import com.KnappTech.sr.model.beans.InvestorBean;
import com.KnappTech.sr.model.comp.Company;
import com.KnappTech.sr.model.comp.Currencies;
import com.KnappTech.sr.model.comp.Currency;
import com.KnappTech.sr.model.constants.CurrencyType;
//import com.KnappTech.sr.model.user.ScoringMethod.*;
//import com.KnappTech.sr.persistence.CompanyManager;
import com.KnappTech.util.Filter;
import com.KnappTech.view.report.Report;
//import com.KnappTech.view.report.ReportRow;

public class Investor
implements KTObject, ReportableSet {
	private static final long serialVersionUID = 201001181249L;
	private final String id;
	private final HashSet<Portfolio> portfolios = new HashSet<Portfolio>();
	private Currencies currencyAvailable = new Currencies();
	private double maxInvestedPercentInSingleStock = 0.15;
	private double personalMarginRequirement = 0.6;
	private double personalShortMarginRequirement = 0.6;
	private double costOfMargin=0.07;
	private double tradingFee = 4.5;
	private ScoringMethod scoringMethod = new ScoringMethod();
	private boolean locked = false;
	private boolean permanentlyLocked = false;
	
	private Investor(String id) {
		this.id = id;
	}
	
	public static final Investor create(String id) {
		if (id!=null && id.length()>0) {
			return new Investor(id);
		}
		return null;
	}

	public static Investor create(InvestorBean bean) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int compareTo(String o) {
		return id.compareTo(o);
	}

	@Override
	public String getID() {
		return id;
	}

//	@Override
//	public void updateReportRow(Filter<Object> instructions,ReportRow<?> row) {
//		// TODO Auto-generated method stub
//	}

	@Override
	public Report<?> getReport(Filter<Object> instructions) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isValid() { return true; }

	public double getDollarsAvailable() {
		return currencyAvailable.getDollarsQuantity();
	}
	
	public Currencies getCurrencyInvested() {
		Currencies currencyInvested = new Currencies();
		for (Portfolio pf : portfolios) {
			Currencies currencyInPF = pf.getCurrencyInvested();
			currencyInvested.addCurrencies(currencyInPF);
		}
		return currencyInvested;
	}
	
	public double getDollarsInvested() {
		return getCurrencyInvested().getDollarsQuantity();
	}
	
	public Currencies getTotalEquity() {
		Currencies c = currencyAvailable.clone();
		c.safelyAddAll(getCurrencyInvested());
		return c;
	}
	
	public void deposit(Currency currency) {
		if (canEdit()) {
			currencyAvailable.add(currency);
		}
	}
	
	public void depositDollars(double amount) {
		if (canEdit()) {
			Currency currency = new Currency(amount,CurrencyType.USDOLLAR);
			currencyAvailable.safelyAdd(currency);
		}
	}

	public void setMaxInvestedPercentInSingleStock(
			double maxInvestedPercentInSingleStock) {
		if (canEdit()) {
			this.maxInvestedPercentInSingleStock = maxInvestedPercentInSingleStock;
		}
	}

	public double getMaxInvestedPercentInSingleStock() {
		return maxInvestedPercentInSingleStock;
	}
	
	public boolean add(Portfolio p) {
		if (canEdit()) {
			return portfolios.add(p);
		}
		return false;
	}

	public void setCostOfMargin(double costOfMargin) {
		if (canEdit()) {
			this.costOfMargin = costOfMargin;
		}
	}

	public double getCostOfMargin() {
		return costOfMargin;
	}

	public int getNumberOfShares(Company company) {
		int numberOfShares = 0;
		for (Portfolio p : portfolios) {
			numberOfShares += p.getNumberOfShares(company);
		}
		return numberOfShares;
	}

	public double getMaxAllowedStockMarketInvestment() {
		double msmi = getTotalEquity().getDollarsQuantity();
		msmi = msmi/personalMarginRequirement;
		return msmi;
	}

	public boolean isWillingToTrade(Company company) {
		for (Portfolio p : portfolios) {
			Position pos = p.getPosition(company);
			if (pos!=null) {
				if (!pos.isWillingToTrade()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isWillingToShort() {
		return getPersonalShortMarginRequirement()<1;
	}

	public double getTradingFee() {
		return tradingFee;
	}
	
	public void setTradingFee(double tradingFee) {
		if (canEdit()) {
			this.tradingFee = tradingFee;
		}
	}
	
	public double getMaxAllowedInvestmentInSingleStock() {
		double maxInvestment = getMaxInvestedPercentInSingleStock()
			*getMaxAllowedStockMarketInvestment();
		return maxInvestment;
	}

	public void setPersonalMarginRequirement(double personalMarginRequirement) {
		if (canEdit()) {
			this.personalMarginRequirement = personalMarginRequirement;
		}
	}

	public double getPersonalMarginRequirement() {
		return personalMarginRequirement;
	}

	public void setPersonalShortMarginRequirement(
			double personalShortMarginRequirement) {
		if (canEdit()) {
			this.personalShortMarginRequirement = personalShortMarginRequirement;
		}
	}

	public double getPersonalShortMarginRequirement() {
		return personalShortMarginRequirement;
	}

	public Collection<String> getCompanyTickers() {
		HashSet<String> tickers = new HashSet<String>();
		for (Portfolio p : portfolios) {
			tickers.addAll(p.getCompanyTickers());
		}
		return tickers;
	}

	public void setScoringMethod(ScoringMethod scoringMethod) {
		if (canEdit()) {
			this.scoringMethod = scoringMethod;
		}
	}

	public ScoringMethod getScoringMethod() {
		return scoringMethod;
	}
	
	public final synchronized boolean isLocked() {
		return locked;
	}
	
	public final synchronized boolean canEdit() {
		return !locked;
	}
	
	public final synchronized void lock() {
		locked = true;
	}
	
	public final synchronized void unlock() {
		if (!permanentlyLocked) {
			locked = false;
		}
	}
	
	public final synchronized void permanentlyLock() {
		permanentlyLocked = true;
		locked = true;
	}
}