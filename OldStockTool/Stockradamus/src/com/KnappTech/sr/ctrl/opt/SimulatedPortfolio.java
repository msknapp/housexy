package com.KnappTech.sr.ctrl.opt;

import java.text.NumberFormat;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import com.KnappTech.sr.model.user.Investor;

public class SimulatedPortfolio {
	private SimulatedPositions positions = null;
	private double transactionCost = 0;
	private double totalNonShortValue = 0;
	private double totalShortValue = 0;
	private double totalValue = 0;
	private double loanValue = 0;
	private double totalCollateralRequired = 0;
	private double totalCollateralAvailable = 0;
	private double collateralRequiredForShortStocks = 0;
	private double collateralRequiredForLongStocks = 0;
	private double finalValue = 0;
	private double finalStandardDeviation = 0;
	private double lowEstimate = 0;
	
	public void setShares(SimulatedPositions positions) {
		this.positions = positions;
	}
	
	public SimulatedPositions getPositions() {
		return positions;
	}
	
	public void setTransactionCost(double transactionCost) {
		this.transactionCost = transactionCost;
	}
	
	public double getTransactionCost() {
		return transactionCost;
	}
	
	public void setFinalValue(double finalValue) {
		if (finalValue<0) {
			finalValue=0;
		}
		this.finalValue = finalValue;
	}
	
	public double getFinalValue() {
		return finalValue;
	}
	
	public void setFinalStandardDeviation(double finalStandardDeviation) {
		this.finalStandardDeviation = finalStandardDeviation;
	}
	
	public double getFinalStandardDeviation() {
		return finalStandardDeviation;
	}
	
	public void setLowEstimate(double lowEstimate) {
		
		this.lowEstimate = lowEstimate;
	}
	
	public double getLowEstimate() {
		return lowEstimate;
	}
	
	public boolean isBetterThan(SimulatedPortfolio otherPortfolio) {
		return (lowEstimate>otherPortfolio.getLowEstimate());
	}
	
	public SimulatedPortfolio spawnChildPortfolio(double maxOffset,Investor investor) {
		boolean acceptable = false;
		SimulatedPortfolio newPortfolio = null;
		double mo = maxOffset;
		while (!acceptable) {
			newPortfolio = new SimulatedPortfolio();
			SimulatedPositions newPositions = positions.spawnChildPositions(investor,mo);
			newPortfolio.setShares(newPositions);
			newPortfolio.evaluate(investor);
			acceptable = newPortfolio.isAcceptable(investor);
			if (mo>5) {
				mo = 0.8*mo;
			}
		}
		return newPortfolio;
	}
	
	public boolean isAcceptable(Investor investor)
	{	// you must have more collateral than is required.
		boolean b1 = totalCollateralAvailable>totalCollateralRequired;
		return (b1);
	}
	
	public SimulatedPortfolios spawnChildPortfolios(double maxOffset,int howMany,Investor investor) {
		SimulatedPortfolios newGuesses = new SimulatedPortfolios();
		double mo = maxOffset;
		for (int i = 0;i<howMany;i++) {
			mo = maxOffset*((double)(i+1)/(double)howMany);
			SimulatedPortfolio newGuess = this.spawnChildPortfolio(mo,investor);
			newGuesses.add(newGuess);
		}
		return newGuesses;
	}

	public void evaluate(Investor investor) {
		double equity = investor.getTotalEquity().getDollarsQuantity();
		transactionCost = positions.getTotalTransactionCost(investor);
		totalShortValue = positions.getTotalShortValue();
		totalNonShortValue = positions.getTotalNonShortValue();
		totalValue = positions.getTotalValue();
		loanValue = computeLoanValue(investor);
		totalCollateralAvailable = computeCollateralValue(investor);
		collateralRequiredForShortStocks = totalShortValue/
				(investor.getPersonalShortMarginRequirement());
		collateralRequiredForLongStocks = Math.max(0, totalNonShortValue-equity)/
				(investor.getPersonalMarginRequirement());
		totalCollateralRequired = collateralRequiredForLongStocks+collateralRequiredForShortStocks;
		finalValue = positions.getEstimatedFinalPortfolioValue();
		finalValue -= loanValue*investor.getCostOfMargin();
		finalStandardDeviation = positions.getFinalStandardDeviation();
		lowEstimate = computeLowEstimate(finalValue,finalStandardDeviation);
	}
	
	private double computeCollateralValue(Investor investor) {
		// as collateral they can use the final net worth of 
		// your portfolio, and any cash you have.
		double initialCash = investor.getDollarsAvailable();
		double finalCash = initialCash-transactionCost;
		double collateral = totalValue+finalCash;
		return collateral;
	}

	public double computeLowEstimate(double finalValue, 
			double finalValueStandardDeviation)
	{
		double lowEstimate = 0;
		if (finalValueStandardDeviation>0) {
			NormalDistributionImpl n = new NormalDistributionImpl
						(finalValue, finalValueStandardDeviation);
			try {
				lowEstimate = n.inverseCumulativeProbability(0.1);
			} catch (MathException e) {
				e.printStackTrace();
			}
		} else {
			return finalValue;
		}
		return lowEstimate;
	}
	
	public void printDetails(Investor investor) {
		double mr = investor.getDollarsAvailable()-getTransactionCost();
		NumberFormat cf = NumberFormat.getCurrencyInstance();
		String str = "Best Solution Details:\n";
		str+=positions.getDetails(investor);
		str+="Cash Before Trades: "+cf.format(investor.getDollarsAvailable())+"\n";
		str+="Cost: "+cf.format(transactionCost)+"\n";
		str+="Cash After Trades: "+cf.format(investor.getDollarsAvailable()-
				transactionCost)+"\n";
		str+="Portfolio Value Before Trades: "+investor.getDollarsInvested()+"\n";
		str+="Collateral Available: "+cf.format(totalCollateralAvailable)+"\n";
		str+="Collateral Required for short position: "+
				cf.format(collateralRequiredForShortStocks)+"\n";
		str+="Collateral Required for long position: "+
				cf.format(collateralRequiredForLongStocks)+"\n";
		str+="Collateral Required: "+cf.format(totalCollateralRequired)+"\n";
		str+="Loan Value: "+cf.format(loanValue)+"\n";
		str+="Total short Value: "+cf.format(totalShortValue)+"\n";
		str+="Total non short Value: "+cf.format(totalNonShortValue)+"\n";
		str+="Total Value: "+cf.format(totalValue)+"\n";
		str+="Estimated Final Value: "+cf.format(finalValue)+"\n";
		str+="Estimated Final Standard Deviation: "+cf.format(finalStandardDeviation)+"\n";
		str+="Low Estimate of Final Value: "+cf.format(lowEstimate)+"\n";
		System.out.println(str);
		System.out.println("Money Remaining: "+cf.format(mr));
		String str2="Max allowed Investment in single stock: "+
			cf.format(investor.getMaxAllowedInvestmentInSingleStock())+"\n";
		System.out.println(str2);
	}

	public double findDistance(SimulatedPortfolio otherPortfolio) {
		return getPositions().findDistance(otherPortfolio.getPositions());
	}
	
	public double findMaxValueDifferenceBetweenTwoPositions(
			SimulatedPortfolio otherPortfolio) {
		return getPositions().findMaxValueDifferenceBetweenTwoPositions(
				otherPortfolio.getPositions());
	}
	
	public double computeMarginPercent(Investor investor) {
		return 1-loanValue/totalCollateralAvailable;
		
	}
	
	public double computeShortMarginPercent(Investor investor) {
		return 1-totalShortValue/totalCollateralAvailable;
	}
	
	public double computeLoanValue(Investor investor) {
		double equity = investor.getTotalEquity().getDollarsQuantity();
		double d = totalShortValue + Math.max(0, totalNonShortValue-equity);
		return d;
	}
}
