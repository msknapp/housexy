package com.KnappTech.sr.persistence;

import com.KnappTech.sr.model.Financial.FinancialEntryType;

public class FinancialEntryTypesManager extends NamedObjectPersistenceManager<FinancialEntryType> {
	private static final String FILENAME = "FinancialEntryTypes.txt";
	private static final FinancialEntryTypesManager instance = new FinancialEntryTypesManager();

	private FinancialEntryTypesManager() {}

	public static int getIndex(String name) {
//		FinancialEntryType.createInstance(name);
		return instance.iGetIndex(name);
	}
	
	public static FinancialEntryType getItem(int index) {
		return instance.iGetItem(index);
	}
	
	public static String getName(int index) {
		return instance.iGetName(index);
	}
	
	public static final void Save() {
		instance.save();
	}
	
	public static final void Load() {
		instance.load();
	}
	
	public static final void LoadIfNecessary() {
		instance.loadIfNecessary();
	}
	
	public static final void Add(FinancialEntryType ind) {
		instance.add(ind);
	}
	
	@Override
	protected String getFileName() {
		return FILENAME;
	}
	
	public static FinancialEntryTypesManager i() {
		return instance;
	}

	@Override
	protected FinancialEntryType instantiate(String name) {
		return FinancialEntryType.getOrCreate(name);
	}
	
	public static final int getSize() {
		return instance.size();
	}

	@Override
	public FinancialEntryType load(String id) {
		loadIfNecessary();
		return FinancialEntryType.get(id);
	}

	@Override
	public void save(FinancialEntryType t) {
		save();
	}

	@Override
	public FinancialEntryType get(String id) {
		loadIfNecessary();
		return FinancialEntryType.get(id);
	}
}