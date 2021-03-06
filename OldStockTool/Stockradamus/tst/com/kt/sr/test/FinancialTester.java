package com.kt.sr.test;

import com.KnappTech.sr.model.Financial.FinancialHistory;
import com.KnappTech.sr.model.Regressable.PriceHistory;
import com.KnappTech.sr.persistence.FinancialEntryTypesManager;
import com.KnappTech.sr.persistence.FinancialHistoryManager;
import com.KnappTech.sr.persistence.PriceHistoryManager;

public class FinancialTester {

	public static void main(String[] args) {
		try {
			FinancialEntryTypesManager.Load();
			FinancialHistory fh = FinancialHistoryManager.getIfStored("COT");
			PriceHistory ph = PriceHistoryManager.getIfStored("COT");
			System.out.println(ph.getBeta());
			double lkp = ph.getLastValue();
			double pe = fh.getPricePerEarnings(lkp).getValue();
			double pm = fh.getProfitMargin().getValue();
			double roa = fh.getReturnOnAssets().getValue();
			double roe = fh.getReturnOnEquity().getValue();
			double ta = fh.getTotalAssets().getValue(); // good.
			double te = fh.getTotalEquity().getValue();
			double tl = fh.getTotalLiabilities().getValue();
			double dlpta = fh.getTotalLiabilitiesPerTotalAssets().getValue();
			double div = fh.getTTMDividend().getValue();
			double dps = fh.getTTMDividendPerShare().getValue();
			double ni = fh.getTTMNetIncome().getValue(); // good
			double ne = fh.getTTMEarnings().getValue(); // good
			double eps = fh.getTTMEPS().getValue(); // good
			double mc = fh.getMarketCapitalization(lkp).getValue();
			double fcfe = fh.getFreeCashFlowToEquity().getValue();
			double g = fh.getWeightedInitialGrowth();
			double i = fh.getInvestorsExpectedReturnRate(ph.getBeta());
			double a = fh.getWeightedFCFE();
			double b = fh.getImpliedYearsRemaining(ph.getBeta(), lkp);
			double be = ph.getBeta();
			double pv = fh.getPresentValue(be, lkp, 15);
			double wfcfe = fh.getWeightedFCFE();
			double wg = fh.getWeightedInitialGrowth();
			
			
			System.out.println("fin");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		double[] x = {1,2,3,4};
//		double[] y = {2,6,6,8};
//		DataPoints dp = new DataPoints(x, y);
//		double a = dp.getSlope();
//		System.out.println(a);
//	}
}