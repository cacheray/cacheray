package types;

public class Struct{
	public final static int STRUCT_READ = 0;
	public final static int STRUCT_WRITE = 1;

	private Address addr; //TODO: types.Address class instead?
	private long size;
	private String name;
	private boolean overflow;
	private StructType structType;
	//private List<MemAccess> memList;

	public Struct(Address addr, long size, String name, StructType structType){ // TODO: does this need typedata? really? honestly, should be given a structtype instead
		this.addr = addr;
		this.size = size;
		this.name = name; // this is really safe
		this.structType = structType;
	//	memList = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	/*public void setStructType(StructType structType) {
		this.structType = structType;
	}*/

	public StructType getStructType() {
		return structType;
	}


	/**
	* Checks if the given address is in the memory marked for this struct
	*
	* @param address - the addr being checked
	*/
	public boolean check(Address address){
		return this.addr.containsAddress(address,size);
	}

	public long getOffset(Address address) {
		return this.addr.getOffset(address);
	}

	/*
	public Object getAccessStats(TypeData typeData) {
		StructType structType = typeData.getStructByName(this.name);

		if(structType == null){
			// No info, should return basic info?
			return null;
		}
		//TODO: Get access stats

		return null;
	}
*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nStruct name: ").append(name)
				.append("\nSize: ").append(size)
				.append("\nAddress: ").append(addr)
				.append(structType != null ? "\nLinked" : "\nNot linked")
				.append("\n");

		return sb.toString();
	}
}