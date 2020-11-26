package types;

// a basic class for tracking the cacheblocks during simulation
public class Block{
	private int size;
	private Address address;
	private int tag;
	private boolean validBit;

	public Block(int size, Address addr, boolean validBit){
		this.size = size;
		this.address = addr;
		this.tag = -1;
		this.validBit = validBit;
	}

	/**
	* Update the block to contain new data. Occurs as the result of a cache read or write
	*
	* @param addr - the addr that marks the start of this cache block. NOTE: this means that the updated address should always be block aligned
	* @param validBit - the value of the valid bit (0 or 1), stored as a boolean
	*/
	public void update(Address addr, boolean validBit){
		this.address = addr;
		this.validBit = validBit;
	}
	
	public int getTag(){
		return tag;
	}

	/**
	* returns the address of this block
	*/
	public Address getAddress(){
		return address;
	}

	/**
	* returns the valid bit of this block
	*/
	public boolean isValid(){
		return validBit;
	}
}