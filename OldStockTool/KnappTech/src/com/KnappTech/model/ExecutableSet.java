package com.KnappTech.model;

public interface ExecutableSet {
	public boolean onStart(Object[] args);
	public boolean onFinish(Object[] args);
	public boolean onStartIteration(Object[] args);
	public boolean onFinishIteration(Object[] args);
	public boolean beforeObject(Object[] args);
	public boolean duringObject(Object[] args);
	public boolean afterObject(Object[] args);
	public void onFault(Exception e, Object[] args);
	public boolean saveWork();
	public boolean reportUpdates();
	public boolean reportTiming();
	public KTSet<? extends KTObject> getSet();
}
